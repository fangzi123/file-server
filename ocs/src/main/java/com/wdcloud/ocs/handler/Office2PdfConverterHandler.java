package com.wdcloud.ocs.handler;

import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.github.tobato.fastdfs.domain.StorePath;
import com.wdcloud.model.entities.FileInfo;
import com.wdcloud.ocs.mq.ConvertModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.DocumentFactoryHelper;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Slf4j
@Component
public class Office2PdfConverterHandler extends AbstractConverterHandler {
    @Autowired
    private ConverterDocument documentConverter;
    private List<String> suffixNames = List.of(
            "doc", "docx", "DOC", "DOCX",
            "xls", "xlsx", "XLS", "XLSX",
            "ppt", "pptx", "PPT", "PPTX"
    );

    @Override
    public void convert(File srcFile, ConvertModel convertModel, FileInfo fileInfo) throws Exception {
        File targetFile = File.createTempFile(UUID.randomUUID().toString(), "." + this.targetExtName());
        try {
            String fileSuffix = com.wdcloud.utils.file.FileUtils.getFileSuffix(fileInfo.getFileId());
            if ("xls".equals(fileSuffix) || "xlsx".equals(fileSuffix) || "XLS".equals(fileSuffix) || "XLSX".equals(fileSuffix)) {
                convertXlsx(srcFile, targetFile, fileSuffix);
            } else {
                documentConverter.convert(srcFile, targetFile);
            }
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

    private void convertXlsx(File srcFile, File targetFile, String fileSuffix) throws Exception {
        //1、获取fileType
        String fileType = fileSuffix;
        //2、获取colWidth
        FileInputStream is = new FileInputStream(srcFile);
        Workbook wb;
        if (POIFSFileSystem.hasPOIFSHeader((InputStream)is)) {
            wb = new HSSFWorkbook((InputStream)is);
        } else {
            if (!DocumentFactoryHelper.hasOOXMLHeader((InputStream)is)) {
                throw new RuntimeException("文档格式不正确!");
            }
            wb = new XSSFWorkbook((InputStream)is);
        }
        Integer colWidth = wb.getSheetAt(0).getRow(0).getPhysicalNumberOfCells();
        //3、获取newDocumentConverter
        SocketOpenOfficeConnection connection = new SocketOpenOfficeConnection("127.0.0.1", 8100);
        ConverterDocument converterDocument = new ConverterDocument(connection, fileType, colWidth);
        //4、转换
        converterDocument.convert(srcFile, targetFile);
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
