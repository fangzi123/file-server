package com.wdcloud.ocs.handler;

import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.wdcloud.model.dao.FileInfoDao;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringJavaAutowiredMembersInspection"})
public abstract class AbstractConverterHandler implements ConverterHandler {
    @Autowired
    public FastFileStorageClient storageClient;
    @Autowired
    public FileInfoDao fileInfoDao;

}
