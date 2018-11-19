package com.wdcloud.oss;

import com.github.tobato.fastdfs.FdfsClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Import;
import org.springframework.jmx.support.RegistrationPolicy;

@Slf4j
@SpringBootApplication
@Import(FdfsClientConfig.class)
// 解决jmx重复注册bean的问题
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
public class OssApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(OssApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("{} is started", this.getClass().getSimpleName());
    }
}
