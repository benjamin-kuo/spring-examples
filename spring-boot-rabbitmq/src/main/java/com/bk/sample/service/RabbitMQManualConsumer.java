package com.bk.sample.service;

import java.io.IOException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import com.bk.sample.config.RabbitConfig;
import com.bk.sample.vo.ProductVo;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RabbitMQManualConsumer {

  @RabbitHandler
  @RabbitListener(queues = {RabbitConfig.QUEUE_MANUEL_NAME})
  public void handleMessage(ProductVo vo, Channel channel, Message message) throws IOException {
    try {
      log.info("processHandler vo：{}", vo);

      switch (vo.getPrice()) {
        case 0:
          log.info("processHandler 模擬處理失敗 vo：{}", vo);
          throw new Exception("模擬處理失敗");
        case 1:
          log.info("processHandler 正常 ack vo：{}", vo);
          channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
          break;
        default:
          log.info("processHandler 不 ack vo：{}", vo);
          break;
      }
    } catch (Exception e) {
      if (message.getMessageProperties().getRedelivered()) {
        log.error("模擬處理再次失敗時, 就不再接受");
        channel.basicReject(message.getMessageProperties().getDeliveryTag(), false); // 拒绝消息
      } else {
        log.error("模擬處理失敗重新丟回Queue");
        channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
      }
    }
  }
}
