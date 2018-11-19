package com.wdcloud.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RabbitListener(queues = MqConfig.FANOUT_C)
public class FanoutReceiverC {

    @RabbitHandler
    public void process(String msg) {
        log.info(msg);
    }

}