package com.wdcloud.ocs.handler;

import com.alibaba.fastjson.JSON;
import com.github.tobato.fastdfs.domain.StorePath;
import com.google.common.collect.Maps;
import com.wdcloud.model.entities.FileInfo;
import com.wdcloud.ocs.mq.ConvertModel;
import com.wdcloud.ocs.util.FfmpegOperations;
import com.wdcloud.ocs.util.MovieInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Slf4j
@Component
public class MovieConverterHandler extends AbstractConverterHandler {

    @Autowired
    private FfmpegOperations ffmpegOperations;
    private List<String> suffixNames = List.of(
            "mp4", "MP4",
            "flv", "FLV"

    );

    @Override
    public void convert(File srcFile, ConvertModel convertModel, FileInfo fileInfo) throws Exception {
        Map<String, String> map = Maps.newHashMap();
        //视频转换
        String targetFilePath = "/tmp/" + UUID.randomUUID().toString().replace("-", "") + ".mp4";
        ffmpegOperations.movie2Mp4(srcFile.getPath(), targetFilePath);
        final File fileMp4 = new File(targetFilePath);
        final StorePath mp4 = storageClient.uploadSlaveFile(convertModel.getGroup(),
                convertModel.getPath(), new FileInputStream(fileMp4), fileMp4.length(), "_mp4", ".mp4");
        map.put("movie", mp4.getFullPath());
        FileUtils.forceDelete(fileMp4);
        MovieInfo movieInfo = ffmpegOperations.getMovieProperty(srcFile.getPath());
        //截图
        targetFilePath = "/tmp/" + UUID.randomUUID().toString().replace("-", "") + ".png";
        ffmpegOperations.movie2thumbnail(srcFile.getPath(), movieInfo.getResolution(), targetFilePath);
        final File fileThumbnail = new File(targetFilePath);
        final StorePath thumbnail = storageClient.uploadSlaveFile(convertModel.getGroup(),
                convertModel.getPath(), new FileInputStream(fileThumbnail), fileThumbnail.length(), "_thumbail", ".png");
        map.put("thumbnail", thumbnail.getFullPath());
        FileUtils.forceDelete(fileThumbnail);
        //缩略图
        fileInfo.setConvertStatus(1);//成功
        fileInfo.setConvertTime(new Date());
        fileInfo.setConvertType(this.targetExtName());
        fileInfo.setConvertResult(JSON.toJSONString(map));
        fileInfoDao.update(fileInfo);
    }

    @Override
    public boolean support(String suffixName) {
        return suffixNames.contains(suffixName);
    }

    @Override
    public String targetExtName() {
        return "mp4";
    }

}
