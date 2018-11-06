package com.wdcloud.file;

import com.alibaba.fastjson.JSON;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.wdcloud.utils.HmacSHA1Utils;
import com.wdcloud.utils.ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringJavaAutowiredFieldsWarningInspection"})
@Slf4j
@RestController
@RequestMapping("file")
public class FileController {
    @Autowired
    private FastFileStorageClient storageClient;

    /**
     * 文件上传
     *
     * @param file 文件
     * @return filePath group1/M00/00/00/wKgFFFvgNp-AeYEzAAAkNqJrSgQ107.jpg
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ResponseDTO fileUpload(@RequestParam("file") MultipartFile file, @RequestParam("token") String token) throws IOException {
        String filename = file.getOriginalFilename();
        assert filename != null;
        String fileExtName = filename.substring(filename.lastIndexOf(".") + 1);
        if (fileExtName.equals(filename)) {
            fileExtName = "unknown";
        }
        validateToken(token, false);
        InputStream inputStream = file.getInputStream();
        StorePath storePath = storageClient.uploadFile(inputStream, file.getSize(), fileExtName, null);
        return ResponseDTO.success(storePath);
    }

//    /**
//     * 下载文件
//     *
//     * @param token    logo.jpg
//     * @param response 下载流
//     */
//    @RequestMapping(value = "/download", method = RequestMethod.GET)
//    public void download(@RequestParam("token") String token, HttpServletResponse response) {
//        //文件名
//        final PathInfo pathInfo = validateToken(token).invoke();
//
//        final String fileName = pathInfo.getName() == null ?
//                pathInfo.getPath().substring(pathInfo.getPath().lastIndexOf(PathInfo.SEPARATOR) + 1) :
//                pathInfo.getName();
//        response.setContentType("application/force-download");
//        response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(fileName, Charset.forName("utf-8")));
//        storageClient.downloadFile(pathInfo.getGroup(), pathInfo.getPath(), new DownloadFileWriter(fileName));
//    }
    ////FdfsConnectionPool.clear() 清理无效连接

    /**
     * 文件信息
     */
    @RequestMapping(value = "/fileInfo", method = RequestMethod.GET)
    public ResponseDTO fileInfo(@RequestParam("token") String token) {
        final PathInfo pathInfo = validateToken(token).invoke();
        return ResponseDTO.success(storageClient.queryFileInfo(pathInfo.getGroup(), pathInfo.getPath()));
    }


    /**
     * 删除文件
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public ResponseDTO delete(@RequestParam("token") String token) {
        final PathInfo pathInfo = validateToken(token).invoke();
        storageClient.deleteFile(pathInfo.getGroup(), pathInfo.getPath());
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
//        jo.put("filePath", "group1/M00/00/00/wKgFFFvhWxaAI9DGAAAkNqJrSgQ994.png");
//        final String encodedPutPolicy = Base64.encodeBase64URLSafeString(jo.toJSONString().getBytes(StandardCharsets.UTF_8));
//        final String sign = genHmacSHA1(encodedPutPolicy, secretKey);
//        System.out.println(secretId + "." + sign + "." + encodedPutPolicy);
//
//    }

    private PathInfo validateToken(String token) {
        return validateToken(token, true);
    }

    private PathInfo validateToken(String token, boolean isParse) {
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
        return isParse ? JSON.parseObject(new String(Base64.decodeBase64(data), StandardCharsets.UTF_8), PathInfo.class) : null;
    }
}
