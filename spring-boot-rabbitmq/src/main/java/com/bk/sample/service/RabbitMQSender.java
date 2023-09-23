package com.bk.sample.service;

import java.nio.charset.StandardCharsets;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bk.sample.config.RabbitConfig;
import com.bk.sample.vo.ProductVo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RabbitMQSender {

	@Autowired
	private RabbitTemplate rabbitTemplate ;

	// 發送到 queue
	public void send(ProductVo vo) {
		rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_NAME, vo);
		log.info("send to queue :{}", vo);
	}

	// 透過 exchange direct 發送
	public void exchangeDirect (String routing, ProductVo vo) {
		rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, routing, vo);

		log.info("send to exchange direct to {}:{}", routing, vo);
	}

	// 透過 exchange fanout 發送
	public void exchangeFanout (ProductVo vo) {
		rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, RabbitConfig.ROUTING_AB, vo);
		log.info("send to exchange with fanout :{}", vo);
	}

	// 透過 exchange topic 發送
	public void exchangeTopic (String routing, ProductVo vo) {
		rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_TOPIC_NAME, routing, vo);
		log.info("send to exchange with topic :{}", vo);
	}

	// 透過 exchange head 發送
	public void exchangeHeader (String header, String data) {
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.setHeader(RabbitConfig.EXCHANGE_HEADER_KEY, header);
		Message message = new Message(data.getBytes(StandardCharsets.UTF_8),messageProperties);
		rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_HEADER_NAME, "", message);
		log.info("send to exchange with header :{}", data);
	}
}
