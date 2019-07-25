package com.wdcloud.ocs.handler;

import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.wdcloud.mq.model.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OfficeConverterConfig {

    @Autowired
    private OpenOfficeConnection connection;

    @Bean
    OpenOfficeConnection openOfficeConnection() {
        return new SocketOpenOfficeConnection();
    }

    @Bean
    ConverterDocument converterDocument() {
        return new ConverterDocument(connection);
    }





}
