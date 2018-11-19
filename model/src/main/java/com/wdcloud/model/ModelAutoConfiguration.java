package com.wdcloud.model;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import tk.mybatis.spring.annotation.MapperScan;

@Configuration
@ComponentScan("com.wdcloud.model")
@ImportResource({
        "classpath:datasource-application.xml"
})
@MapperScan(basePackages = {
        "com.wdcloud.model.mapper",
})
public class ModelAutoConfiguration {
}
