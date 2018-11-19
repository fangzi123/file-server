package com.wdcloud.oss;

import com.github.tobato.fastdfs.domain.StorePath;
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
        final StorePath path = StorePath.praseFromUrl(filePath);
        this.group = path.getGroup();
        this.path = path.getPath();
        return this;
    }
}