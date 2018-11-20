package com.wdcloud.oss;

import com.wdcloud.mq.model.ConvertMQO;
import com.wdcloud.mq.model.MqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConvertSender {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public void send(ConvertMQO mqo) {
        threadPoolTaskExecutor.execute(() -> {
            try {
                Thread.sleep(500);//TODO 转码任务 //TODO 异常扩展
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            amqpTemplate.convertAndSend(MqConstants.TOPIC_EXCHANGE_OSS_CONVERT, MqConstants.QUEUE_OSS_CONVERT, mqo);
        });
    }
}
