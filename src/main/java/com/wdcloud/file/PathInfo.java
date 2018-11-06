package com.wdcloud.file;

import lombok.Data;

@Data
public class PathInfo {
    private String group;
    private String path;
    private String filePath;
    private String version;
    private long timestamp;
    public static final String SEPARATOR = "/";

    public PathInfo invoke() {
        if (filePath.startsWith(SEPARATOR)) {
            filePath = filePath.replaceFirst(SEPARATOR, "");
        }
        final int index = filePath.indexOf(SEPARATOR);
        group = filePath.substring(0, index);
        path = filePath.substring(index).replaceFirst(SEPARATOR, "");
        return this;
    }
}