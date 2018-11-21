package com.wdcloud.ocs.util;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class FfmpegOperations {
    private String regexDuration = "Duration: (.*?), start: (.*?), bitrate: (\\d*) kb\\/s";
    private String regexVideo = "Video: (.*?), (.*?), (.*?)[,\\s]";

//    private String regexAudio = "Audio: (\\w*), (\\d*) Hz";

    @Value("${ffmpeg.home}")
    private String ffmpegHome;

    public MovieInfo getMovieProperty(String input) {
        List<String> command = new ArrayList<>();
        command.add(ffmpegHome);
        command.add("-i");
        command.add(input);
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(command);
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = buf.readLine()) != null) {
                sb.append(line);
            }
            buf.close();
            MovieInfo movieInfo = new MovieInfo();
            Pattern compile = Pattern.compile(regexDuration);
            Matcher matcher = compile.matcher(sb);
            if (matcher.find()) {
                movieInfo.setDuration(matcher.group(1));
                movieInfo.setStart(matcher.group(2));
                movieInfo.setBitrate(matcher.group(3));
            }
            compile = Pattern.compile(regexVideo);
            matcher = compile.matcher(sb);
            if (matcher.find()) {
                movieInfo.setResolution(matcher.group(3));
            }
            return movieInfo;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void movie2Mp4(String input, String output) throws Exception {
        List<String> command = new ArrayList<String>();
        command.add(ffmpegHome); // 添加转换工具路径
        command.add("-loglevel"); // 设定命令日志级别
        command.add("error"); // 设定命令日志级别
        command.add("-y"); // 添加参数＂-y＂，该参数指定将覆盖已存在的文件
        command.add("-i"); // 添加参数＂-i＂，该参数指定要转换的文件
        command.add(input); // 添加要转换格式的视频文件的路径
        command.add("-c:v"); // 设置视频编码
        command.add("libx264"); // 设置视频编码
        command.add("-c:a"); // 设置音频编码
        command.add("aac"); // 设置音频编码
        command.add("-metadata"); // 设置元信息标题
        command.add("title=\"wdcloud.video\"");
        command.add("-metadata"); // 设置元信息内容
        command.add("comment=\"wdcloud.video\"");
        command.add("-x264opts"); // 设置x264编码属性，关键帧设置
        command.add("keyint=250:min-keyint=25");

        command.add("-preset"); // 设置帧频
        command.add("superfast"); // 设置帧频
        command.add("-r"); // 设置帧频
        command.add("25"); // 设置帧频
        command.add("-f"); // 输出文件格式
        command.add("mp4"); // 输出文件格式
        command.add("-movflags"); // 将元信息从文件尾部移到头部,播放加速优化
        command.add("faststart");
        command.add(output);
        exec(command);
    }

    public void movie2thumbnail(String input, String widthxheight, String output) throws Exception {
        List<String> command = new ArrayList<String>();
        command.add(ffmpegHome); // 添加转换工具路径
        command.add("-loglevel"); // 设定命令日志级别
        command.add("error"); // 设定命令日志级别
        command.add("-y"); // 添加参数＂-y＂，该参数指定将覆盖已存在的文件
        command.add("-t"); // 截取1毫秒
        command.add("0.001"); // 截取1毫秒
        command.add("-i"); // 添加参数＂-i＂，该参数指定要转换的文件
        command.add(input); // 添加要转换格式的视频文件的路径
        command.add("-f"); // 输出文件格式
        command.add("image2"); // 输出文件格式
        command.add("-s");
        command.add(widthxheight);
        command.add(output);
        exec(command);
    }

    public void movie2thumbnail(String input, String output) throws Exception {
        movie2thumbnail(input, "640*480", output);
    }

    private void exec(List<String> command) throws Exception {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);
        builder.redirectErrorStream(true);
        Process process = null;
        try {
            process = builder.start();
            //为了保险起见，在读出的时候，最好把子进程的输出流和错误流都读出来，这样可以保证清空缓存区。
            new Print(process.getInputStream()).run();
            new Print(process.getErrorStream()).run();
            process.waitFor();
        } catch (Exception e) {
            log.error(Throwables.getStackTraceAsString(e));
            throw new RuntimeException(e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    class Print extends Thread {

        private InputStream inputStream;

        Print(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            if (inputStream != null) {
                BufferedReader stdout = new BufferedReader(new InputStreamReader(inputStream));
                try {
                    String line;
                    while ((line = stdout.readLine()) != null) {
                        log.info(line);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        stdout.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
