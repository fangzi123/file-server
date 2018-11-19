package com.wdcloud.file;

import com.wdcloud.utils.ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringJavaAutowiredFieldsWarningInspection"})
@Slf4j
@RestController
@RequestMapping("mq")
public class MqController {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    /**
     *
     */
    @RequestMapping(value = "/queue", method = RequestMethod.GET)
    public ResponseDTO queue() throws IOException {
        rabbitTemplate.convertAndSend(MqConfig.QUEUE_HELLO,"数据");
        return ResponseDTO.success();
    }
    /**
     *
     *
     * @return filePath group1/M00/00/00/wKgFFFvgNp-AeYEzAAAkNqJrSgQ107.jpg
     */
    @RequestMapping(value = "/topic", method = RequestMethod.GET)
    public ResponseDTO topic() throws IOException {
        this.rabbitTemplate.convertAndSend(MqConfig.TOPIC_EXCHANGE, MqConfig.TOPIC_MESSAGES, MqConfig.TOPIC_MESSAGES);
        return ResponseDTO.success();
    }
    /**
     *
     *
     * @return filePath group1/M00/00/00/wKgFFFvgNp-AeYEzAAAkNqJrSgQ107.jpg
     */
    @RequestMapping(value = "/topic2", method = RequestMethod.GET)
    public ResponseDTO topic2() throws IOException {
        this.rabbitTemplate.convertAndSend( MqConfig.TOPIC_EXCHANGE,MqConfig.TOPIC_MESSAGE, MqConfig.TOPIC_MESSAGE);
        return ResponseDTO.success();
    }
    /**
     *
     *
     * @return filePath group1/M00/00/00/wKgFFFvgNp-AeYEzAAAkNqJrSgQ107.jpg
     */
    @RequestMapping(value = "/fanout", method = RequestMethod.GET)
    public ResponseDTO fanout() throws IOException {
        this.rabbitTemplate.convertAndSend(MqConfig.FANOUT_EXCHANGE,"随便指定,因为是广播模式", "xxxxxxx");
        return ResponseDTO.success();
    }

}
