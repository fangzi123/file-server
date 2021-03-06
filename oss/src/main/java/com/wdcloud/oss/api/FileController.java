package com.wdcloud.oss.api;

import com.alibaba.fastjson.JSON;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.google.common.base.Throwables;
import com.wdcloud.model.dao.FileInfoDao;
import com.wdcloud.model.entities.FileInfo;
import com.wdcloud.mq.model.ConvertMQO;
import com.wdcloud.oss.model.Constants;
import com.wdcloud.oss.model.Parm;
import com.wdcloud.oss.mq.ConvertSender;
import com.wdcloud.utils.HmacSHA1Utils;
import com.wdcloud.utils.ResponseDTO;
import com.wdcloud.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringJavaAutowiredFieldsWarningInspection"})
@Slf4j
@RestController
@RequestMapping("file")
public class FileController {
    public static final String SEPARATOR = "/";
    @Autowired
    private FastFileStorageClient storageClient;
    @Autowired
    private ConvertSender convertSender;
    @Autowired
    private FileInfoDao fileInfoDao;

    /**
     * 文件上传
     *
     * @param file 文件
     * @return filePath group1/M00/00/00/wKgFFFvgNp-AeYEzAAAkNqJrSgQ107.jpg
     */
    @PostMapping(value = "/upload")
    public ResponseDTO fileUpload(@RequestParam("file") MultipartFile file, @RequestParam("token") String token) throws IOException {
        String filename = file.getOriginalFilename();
        assert filename != null;
        String fileExtName = filename.substring(filename.lastIndexOf(".") + 1);
        if (fileExtName.equals(filename)) {
            return ResponseDTO.notOK("The file suffix is invalid");
        }
        //TOKEN 校验
        validateToken(token, false);
        InputStream inputStream = file.getInputStream();
        StorePath storePath = storageClient.uploadFile(inputStream, file.getSize(), fileExtName, null);
        log.info(JSON.toJSONString(storePath));
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileId(storePath.getFullPath());
        fileInfo.setFileSize(file.getSize());
        fileInfo.setFileType(fileExtName);
        fileInfo.setOriginName(filename);
        fileInfoDao.save(fileInfo);
        ConvertMQO mqo = new ConvertMQO();
        mqo.setFileId(storePath.getFullPath());
        convertSender.send(mqo);
        return ResponseDTO.success(fileInfo);
    }

    /**
     * 获取byte流
     *
     * @param inputStream
     * @return
     */

    /**
     * 下载文件
     *
     * @param token    logo.jpg
     * @param response 下载流
     */
    @GetMapping(value = "/download")
    public void download(@RequestParam("token") String token, @RequestParam(value = "name", required = false) String name, HttpServletResponse response) throws IOException {
        //文件名
        final Parm parm = validateToken(token);

        String fileName;
        if (StringUtil.isNotEmpty(name)) {
            fileName = name + parm.getFileId().substring(parm.getFileId().lastIndexOf("."));
        } else {
            fileName = parm.getFileId().substring(parm.getFileId().lastIndexOf(SEPARATOR) + 1);
        }
        response.reset();
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(fileName, Charset.forName("utf-8")));
        byte[] bytes = storageClient.downloadFile(parm.getFileId().substring(0, parm.getFileId().indexOf(SEPARATOR)), parm.getFileId().substring(parm.getFileId().indexOf(SEPARATOR) + 1), new DownloadByteArray());
        write(response.getOutputStream(), bytes);
    }
    ////FdfsConnectionPool.clear() 清理无效连接

    /**
     * 文件信息
     */
    @GetMapping(value = "/fileInfo")
    public ResponseDTO fileInfo(@RequestParam("token") String token) {
        final Parm parm = validateToken(token);
        final FileInfo one = fileInfoDao.findOne(FileInfo.builder().fileId(parm.getFileId()).build());
        if (one == null) {
            return ResponseDTO.notOK(null, "无效的文件ID");
        }
        return ResponseDTO.success(one);
    }


    /**
     * 删除文件
     */
    @PostMapping(value = "/delete")
    public ResponseDTO delete(@RequestParam("token") String token) {
        final Parm parm = validateToken(token);
        final StorePath storePath = StorePath.praseFromUrl(parm.getFileId());
        storageClient.deleteFile(storePath.getGroup(), storePath.getPath());
        //从文件删除
        return ResponseDTO.success();
    }


//    public static void main(String[] args) {
//        /**
//         * 签名基本原理是通过 key/secret 的实现：
//         * 1, 服务器负责为每个客户端生成一对 key/secret （ key/secret 没有任何关系，不能相互推算），保存，并告知客户端。
//         * 2, 当客户端调用 api 时，根据某种规则将所有请求参数串联起来并用 secret 生成签名 sign 。
//         * 3, 将 sign 和 key 一起放进请求参数对服务器进行调用。（注意 secret 不要传）
//         * 4, 服务端收到请求，根据 key 去查 secret ，然后用同样的算法，验证签名。
//         * 5, 为避免重放攻击，可加上 timestamp 参数，指明客户端调用的时间。服务端在验证请求时若 timestamp 超过允许误差则直接返回错误。
//         */
//        //	AccessKey/SecretKey
//        //key : 12b517e983a945089243a177f2097a1d
//        //secret : be5b60e5483d43da8dd550ab3dbccc74
//        String secretId = "12b517e983a945089243a177f2097a1d";
//        String secretKey = "be5b60e5483d43da8dd550ab3dbccc74";
//
////        encodedPutPolicy
//        JSONObject jo = new JSONObject();
//        jo.put("timestamp", 1451491200);
//        jo.put("version", 1);
//        jo.put("fileId", "group1/M00/00/0C/wKgFFFx30XyAYKAkAAABkbzGGrg696.csv");
//        final String encodedPutPolicy = Base64.encodeBase64URLSafeString(jo.toJSONString().getBytes(StandardCharsets.UTF_8));
//        final String sign = HmacSHA1Utils.genHmacSHA1WithEncodeBase64URLSafe(encodedPutPolicy, secretKey);
//        System.out.println(secretId + "." + sign + "." + encodedPutPolicy);
//    }

    private Parm validateToken(String token) {
        return validateToken(token, true);
    }

    private Parm validateToken(String token, boolean isParse) {
        final String[] tokens = token.split("\\.");
        if (tokens.length != 3) {
            throw new RuntimeException("invalid token");
        }
        final String key = Constants.keys.get(tokens[0]);
        final String data = tokens[2];
        final String sign = HmacSHA1Utils.genHmacSHA1WithEncodeBase64URLSafe(data, key);
        if (!tokens[1].equals(sign)) {
            throw new RuntimeException("invalid token");
        }
        return isParse ? JSON.parseObject(new String(Base64.decodeBase64(data), StandardCharsets.UTF_8), Parm.class) : null;
    }

    private void write(OutputStream outputStream, byte[] bytes) throws IOException {

        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        try {
            bos = new BufferedOutputStream(outputStream);
            bis = new BufferedInputStream(new ByteArrayInputStream(bytes), bytes.length);
            byte[] buff = new byte[2048];
            int bytesRead;
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
            }
        } catch (Exception e) {
            log.error("Exception={}", Throwables.getStackTraceAsString(e));
            throw e;
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
            } finally {
                if (bos != null) {
                    bos.close();
                }
            }
        }
    }
}
