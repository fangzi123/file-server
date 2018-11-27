package com.wdcloud.model.dao;

import com.wdcloud.model.entities.FileInfo;
import com.wdcloud.model.mapper.ext.FileInfoExtMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class FileInfoDao extends CommonDao<FileInfo, Long> {
    @Autowired
    private FileInfoExtMapper extMapper;

    public void saveErrorMsg(Map<String,Object> param){
        extMapper.saveErrorMsg(param);
    }
    @Override
    protected Class<FileInfo> getBeanClass() {
        return FileInfo.class;
    }
}
