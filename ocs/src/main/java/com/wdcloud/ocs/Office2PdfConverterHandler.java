package com.wdcloud.ocs;

import lombok.extern.slf4j.Slf4j;
import org.jodconverter.DocumentConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Slf4j
@Component
public class Office2PdfConverterHandler implements ConverterHandler {
    @Autowired
    private DocumentConverter documentConverter;
    private List<String> suffixNames = List.of("doc", "docx", "xls", "xlsx", "ppt", "pptx");

    @Override
    public void convert(File srcFile, File targetFile, ConvertModel convertModel) throws Exception {
        documentConverter.convert(srcFile).to(targetFile).execute();
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
