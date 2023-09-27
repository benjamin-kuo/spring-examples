package com.bk.sample.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.PendingConfirm;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import com.bk.sample.config.RabbitConfig;
import com.bk.sample.vo.ProductVo;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RabbitMQConsumer {

  @Autowired
  private RabbitTemplate rabbitTemplate ;

  @RabbitListener(queues = {RabbitConfig.QUEUE_NAME})
  public void receivedMessage(ProductVo vo) {
    log.info("consume vo:{}", vo.getProductId());
  }

  // 取得要 retry queue 中的 message
  public void receiveRetry() {
    ProductVo vo = (ProductVo) rabbitTemplate.receiveAndConvert(RabbitConfig.QUEUE_RETRY);
    log.info("Receive retry vo:{}", vo.getProductId());
  }
}
