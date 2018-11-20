package com.wdcloud.ocs;

import cn.hutool.core.util.IdUtil;
import com.github.tobato.fastdfs.domain.StorePath;
import com.wdcloud.model.entities.FileInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jodconverter.DocumentConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.List;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Slf4j
@Component
public class Office2PdfConverterHandler extends AbstractConverterHandler {
    @Autowired
    private DocumentConverter documentConverter;
    private List<String> suffixNames = List.of(
            "doc", "docx", "DOC", "DOCX",
            "xls", "xlsx", "XLS", "XLSX",
            "ppt", "pptx", "PPT", "PPTX"
    );

    @Override
    public void convert(File srcFile, ConvertModel convertModel, FileInfo fileInfo) throws Exception {
        File targetFile = File.createTempFile(IdUtil.simpleUUID(), "." + this.targetExtName());
        try {
            documentConverter.convert(srcFile).to(targetFile).execute();
            final StorePath slaveFile = storageClient.uploadSlaveFile(convertModel.getGroup(),
                    convertModel.getPath(),
                    new FileInputStream(targetFile),
                    targetFile.length(),
                    "_" + this.targetExtName(),
                    "." + this.targetExtName());
            fileInfo.setConvertStatus(1);//成功
            fileInfo.setConvertTime(new Date());
            fileInfo.setConvertType(this.targetExtName());
            fileInfo.setConvertResult(slaveFile.getFullPath());
            fileInfoDao.update(fileInfo);
        } finally {
            FileUtils.forceDelete(targetFile);
        }
    }

    @Override
    public boolean support(String suffixName) {
        return suffixNames.contains(suffixName);
    }

    @Override
    public String targetExtName() {
        return "pdf";
    }

}
