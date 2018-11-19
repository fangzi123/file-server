package com.wdcloud.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 一对一
 */
@Slf4j
@Component
@RabbitListener(queues =MqConfig.QUEUE_HELLO)
public class HelloReceiver2 {

    @RabbitHandler
    public void process(String msg) {
        log.info(msg);
    }

}