package com.wdcloud.ocs;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.proto.storage.DownloadFileWriter;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.google.common.base.Throwables;
import com.wdcloud.model.ConvertMQO;
import com.wdcloud.model.MqConstants;
import com.wdcloud.model.dao.FileInfoDao;
import com.wdcloud.model.entities.FileInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringJavaAutowiredFieldsWarningInspection"})
@Slf4j
@Component
public class ConverterReceiver {
    @Autowired
    private ConverterHandlerFactory converterHandlerFactory;
    @Autowired
    private FastFileStorageClient storageClient;
    @Autowired
    private FileInfoDao fileInfoDao;
    @Autowired
    private CleanTmpFileTask cleanTmpFileTask;

    @RabbitListener(queues = MqConstants.QUEUE_OSS_CONVERT)
    public void process(ConvertMQO mqo) {
        log.info("converter invoke");
        final ConverterHandler handler = converterHandlerFactory.bySuffixName(FileUtil.extName(mqo.getFileId()));
        if (handler == null) {
            log.warn("没有对应的处理器,{},{}", mqo.getFileId(), FileUtil.extName(mqo.getFileId()));
            return;
        }
        final StorePath storePath = StorePath.praseFromUrl(mqo.getFileId());
        ConvertModel convertModel = new ConvertModel();
        convertModel.setFileId(mqo.getFileId());
        BeanUtil.copyProperties(storePath, convertModel);
        final FileInfo one = fileInfoDao.findOne(FileInfo.builder().fileId(convertModel.getFileId()).build());
        if (one == null) {
            log.error("文件不存在");
            return;
        }
        File srcFile = null;
        try {
            String srcFilePath = storageClient.downloadFile(storePath.getGroup(), storePath.getPath(), new DownloadFileWriter("/tmp/" + storePath.getPath().substring(storePath.getPath().lastIndexOf("/") + 1)));
            srcFile = new File(srcFilePath);
        } catch (Exception e) {
            //文件下载异常
            log.error(Throwables.getStackTraceAsString(e));
            retry(one, e);
        }
        File targetFile = null;
        try {
            targetFile = File.createTempFile(IdUtil.simpleUUID(), "." + handler.targetExtName());
        } catch (Exception e) {
            //创建临时目录失败
            log.error(Throwables.getStackTraceAsString(e));
            retry(one, e);
        }
        if (srcFile == null || targetFile == null) {
            return;
        }
        //保存转换信息
        try {
            handler.convert(srcFile, targetFile, convertModel);
            final StorePath slaveFile = storageClient.uploadSlaveFile(convertModel.getGroup(),
                    convertModel.getPath(),
                    new FileInputStream(targetFile),
                    targetFile.length(),
                    "_" + handler.targetExtName(),
                    "." + handler.targetExtName());
            one.setConvertStatus(1);//成功
            one.setConvertTime(new Date());
            one.setConvertType(handler.targetExtName());
            one.setConvertResult(slaveFile.getFullPath());
            fileInfoDao.update(one);
        } catch (Exception e) {
            retry(one, e);
        } finally {
            try {
                FileUtils.forceDelete(srcFile);
                FileUtils.forceDelete(targetFile);
            } catch (IOException e) {
                //
            }
            log.info("converter end");
        }

    }

    private void retry(FileInfo one, Exception e) {
        if (one.getConvertCount() <= 3) {
            one.setConvertStatus(-1);//失败
            one.setConvertErrorMsg(e.getMessage());
            one.setConvertCount(one.getConvertCount() + 1);
            log.error(Throwables.getStackTraceAsString(e));
            fileInfoDao.update(one);
            throw new RuntimeException(e);
        } else {
            log.error("{} retry is {} count", one.getFileId(), one.getConvertCount());
        }
    }

    public static void main(String[] args) {
    }
}