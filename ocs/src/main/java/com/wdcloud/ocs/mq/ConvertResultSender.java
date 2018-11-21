package com.wdcloud.ocs.mq;

import com.wdcloud.mq.model.ConvertResultMQO;
import com.wdcloud.mq.model.MqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConvertResultSender {

    @Autowired
    private AmqpTemplate amqpTemplate;

    public void send(ConvertResultMQO mqo) {
        amqpTemplate.convertAndSend(MqConstants.TOPIC_EXCHANGE_LMS, MqConstants.QUEUE_LMS_CONVERT_RESULT, mqo);
    }
}
