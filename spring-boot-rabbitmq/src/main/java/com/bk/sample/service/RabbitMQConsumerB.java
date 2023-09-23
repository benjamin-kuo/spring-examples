package com.bk.sample.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import com.bk.sample.config.RabbitConfig;
import com.bk.sample.vo.ProductVo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RabbitMQConsumerB {

  @RabbitListener(queues = {RabbitConfig.QUEUE_B})
  public void receivedMessage(ProductVo vo) {
    log.info("consume vo:{}", vo.getProductId());
  }
}
