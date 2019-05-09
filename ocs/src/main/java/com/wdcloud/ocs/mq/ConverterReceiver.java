package com.wdcloud.ocs.mq;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.proto.storage.DownloadFileWriter;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.wdcloud.model.dao.FileInfoDao;
import com.wdcloud.model.entities.FileInfo;
import com.wdcloud.mq.model.ConvertMQO;
import com.wdcloud.mq.model.MqConstants;
import com.wdcloud.ocs.handler.ConverterHandler;
import com.wdcloud.ocs.handler.ConverterHandlerFactory;
import com.wdcloud.utils.BeanUtil;
import com.wdcloud.utils.file.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
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

    @RabbitListener(bindings = {@QueueBinding(value = @Queue(MqConstants.QUEUE_OSS_CONVERT),exchange = @Exchange(type = "topic",value=MqConstants.TOPIC_EXCHANGE_OSS_CONVERT))})
    public void process(ConvertMQO mqo) throws Exception {
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
        String srcFilePath = storageClient.downloadFile(
                storePath.getGroup(),
                storePath.getPath(),
                new DownloadFileWriter("/tmp/" + storePath.getPath().substring(storePath.getPath().lastIndexOf("/") + 1)));
        File srcFile = new File(srcFilePath);
        handler.convert(srcFile, convertModel, fileInfo);
        try {
            org.apache.commons.io.FileUtils.forceDelete(srcFile);
        } catch (IOException e) {

        }
        log.info("converter end");
    }
}