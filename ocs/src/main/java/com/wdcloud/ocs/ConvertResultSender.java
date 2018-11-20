package com.wdcloud.ocs;

import com.wdcloud.mq.model.ConvertResultMQO;
import com.wdcloud.mq.model.MqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConvertResultSender {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public void send(ConvertResultMQO mqo) {
        threadPoolTaskExecutor.execute(() -> {
            try {
                Thread.sleep(500);//TODO 转码任务 //TODO 异常扩展
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            amqpTemplate.convertAndSend(MqConstants.TOPIC_EXCHANGE_LMS, MqConstants.QUEUE_LMS_CONVERT_RESULT, mqo);
        });
    }
}
