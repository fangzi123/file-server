package com.wdcloud.model.dao;

import com.wdcloud.model.entities.FileInfo;
import org.springframework.stereotype.Repository;

@Repository
public class FileInfoDao extends CommonDao<FileInfo, Long> {
    @Override
    protected Class<FileInfo> getBeanClass() {
        return FileInfo.class;
    }
}
