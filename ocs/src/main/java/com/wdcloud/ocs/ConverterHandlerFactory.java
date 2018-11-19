package com.wdcloud.ocs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Component
public class ConverterHandlerFactory {

    @Autowired
    private ConverterHandler[] converterHandlers;

    public ConverterHandler bySuffixName(String suffixName) {
        for (ConverterHandler handler : converterHandlers) {
            if (handler.support(suffixName)) {
                return handler;
            }
        }
        return null;
    }


}
