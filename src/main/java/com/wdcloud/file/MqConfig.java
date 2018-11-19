package com.wdcloud.file;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class MqConfig {
    public static final String QUEUE_HELLO = "queue.hello";
    public static final String TOPIC_MESSAGE = "topic.message";
    public static final String TOPIC_MESSAGES = "topic.1messages";
    public static final String TOPIC_EXCHANGE = "TopicExchange";
    public static final String FANOUT_EXCHANGE = "fanoutExchange";
    public static final String FANOUT_A = "fanout.A";
    public static final String FANOUT_B = "fanout.B";
    public static final String FANOUT_C = "fanout.C";

    @Bean
    public Queue helloQueue() {
        return new Queue(MqConfig.QUEUE_HELLO);
    }

    //===============以下是验证topic Exchange的队列==========
    @Bean
    public Queue queueMessage() {
        return new Queue(TOPIC_MESSAGE);
    }

    @Bean
    public Queue queueMessages() {
        return new Queue(TOPIC_MESSAGES);
    }
    //===============以上是验证topic Exchange的队列==========


    //===============以下是验证Fanout Exchange的队列==========
    @Bean
    public Queue AMessage() {
        return new Queue(FANOUT_A);
    }

    @Bean
    public Queue BMessage() {
        return new Queue(FANOUT_B);
    }

    @Bean
    public Queue CMessage() {
        return new Queue(FANOUT_C);
    }
    //===============以上是验证Fanout Exchange的队列==========


    @Bean
    TopicExchange exchange() {
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    @Bean
    FanoutExchange fanoutExchange() {
        return new FanoutExchange(FANOUT_EXCHANGE);
    }

    /**
     * 将队列topic.message与exchange绑定，binding_key为topic.message,就是完全匹配
     *
     * @param queueMessage
     * @param exchange
     * @return
     */
    @Bean
    Binding bindingExchangeMessage(Queue queueMessage, TopicExchange exchange) {
        return BindingBuilder.bind(queueMessage).to(exchange).with(TOPIC_MESSAGE);
    }

    /**
     * 将队列topic.messages与exchange绑定，binding_key为topic.#,模糊匹配
     *
     * @param queueMessages queueMessages
     * @param exchange
     * @return
     */
    @Bean
    Binding bindingExchangeMessages(Queue queueMessages, TopicExchange exchange) {
        return BindingBuilder.bind(queueMessages).to(exchange).with("topic.#");
    }

    @Bean
    Binding bindingExchangeA(Queue AMessage, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(AMessage).to(fanoutExchange);
    }

    @Bean
    Binding bindingExchangeB(Queue BMessage, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(BMessage).to(fanoutExchange);
    }

    @Bean
    Binding bindingExchangeC(Queue CMessage, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(CMessage).to(fanoutExchange);
    }
}
