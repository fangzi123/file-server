package com.wdcloud.ocs;

import java.io.File;

/**
 * 文件转换接口
 */
public interface ConverterHandler {

    /**
     * 文件转换
     *
     * @param srcFile      the src file
     * @param convertModel convertModel
     */
    void convert(File srcFile, File targetFile, ConvertModel convertModel) throws Exception;

    /**
     * 支持的类别 e.g.: jpg
     *
     * @param suffixName 文件扩展名称
     * @return boolean
     */
    boolean support(String suffixName);

    /**
     * 目标扩展名 e.g.: png
     *
     * @return
     */
    String targetExtName();


}