package com.wdcloud.ocs.mq;

import com.wdcloud.mq.model.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfig {

    @Bean
    Queue queue() {
        return new Queue(MqConstants.QUEUE_LMS_CONVERT_RESULT);
    }

    @Bean
    TopicExchange topicExchange() {
        return new TopicExchange(MqConstants.TOPIC_EXCHANGE_LMS);
    }

    @Bean
    Binding bindingExchangeOssConvert(Queue queue,TopicExchange topicExchange) {
        return BindingBuilder
                .bind(queue)
                .to(topicExchange)
                .with(MqConstants.QUEUE_LMS_CONVERT_RESULT);
    }

}
