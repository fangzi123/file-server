package com.wdcloud.ocs;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Slf4j
@Component
public class CleanTmpFileTask implements InitializingBean {
    private BlockingQueue<File> queue = new ArrayBlockingQueue<>(36);

    public void add(File file) {
        queue.add(file);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(() -> {
            try {
                final File take = queue.take();
                FileUtils.forceDelete(take);
                System.out.println("=========="+take.getName());
            } catch (Exception e) {
                log.error(" del error", Throwables.getStackTraceAsString(e));
            }
        }).start();
    }
}
