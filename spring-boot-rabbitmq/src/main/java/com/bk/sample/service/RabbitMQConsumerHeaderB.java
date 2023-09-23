package com.bk.sample.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import com.bk.sample.config.RabbitConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RabbitMQConsumerHeaderB {

  @RabbitListener(queues = {RabbitConfig.QUEUE_HEADER_B})
  public void receivedMessage(String data) {
    log.info("consume : {}", data);
  }
}
