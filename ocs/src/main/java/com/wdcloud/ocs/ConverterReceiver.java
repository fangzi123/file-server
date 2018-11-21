package com.wdcloud.ocs;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.proto.storage.DownloadFileWriter;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.google.common.base.Throwables;
import com.wdcloud.model.dao.FileInfoDao;
import com.wdcloud.model.entities.FileInfo;
import com.wdcloud.mq.model.ConvertMQO;
import com.wdcloud.mq.model.ConvertResultMQO;
import com.wdcloud.mq.model.MqConstants;
import com.wdcloud.utils.BeanUtil;
import com.wdcloud.utils.file.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

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
    private ConvertResultSender convertResultSender;

    @RabbitListener(queues = MqConstants.QUEUE_OSS_CONVERT)
    public void process(ConvertMQO mqo) {
        log.info("converter invoke");

        final ConverterHandler handler = converterHandlerFactory.bySuffixName(FileUtils.getFileSuffix(mqo.getFileId()));
        if (handler == null) {
            log.warn("没有对应的处理器,{},{}", mqo.getFileId(), FileUtils.getFileSuffix(mqo.getFileId()));
            return;
        }
        final StorePath storePath = StorePath.praseFromUrl(mqo.getFileId());
        ConvertModel convertModel = new ConvertModel();
        convertModel.setFileId(mqo.getFileId());
        BeanUtil.copyProperties(storePath, convertModel);
        final FileInfo fileInfo = fileInfoDao.findOne(FileInfo.builder().fileId(convertModel.getFileId()).build());
        if (fileInfo == null) {
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
            retry(fileInfo, e);
        }
        //保存转换信息
        if (srcFile == null) {
            return;
        }
        try {
            handler.convert(srcFile, convertModel, fileInfo);
        } catch (Exception e) {
            retry(fileInfo, e);
        }
        try {
            org.apache.commons.io.FileUtils.forceDelete(srcFile);
        } catch (IOException e) {
            //
        }
        log.info("converter end");

        //TODO 区分发到哪里 可扩展
        ConvertResultMQO resultMQO = new ConvertResultMQO();
        BeanUtil.copyProperties(fileInfo, resultMQO);
        convertResultSender.send(resultMQO);
    }

    private void retry(FileInfo one, Exception e) {
        if (one.getConvertCount() < 3) {
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
}