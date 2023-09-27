package com.bk.sample.service;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;
import com.bk.sample.component.RedisStreamComponent;
import com.bk.sample.config.RedisStreamConfig;
import com.bk.sample.vo.ProductVo;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class RedisStreamBService implements StreamListener<String, ObjectRecord<String, ProductVo>> {

  @Value("${server.port:8080}")
  int applicationPort;

  private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final RedisStreamComponent redisStreamComponent;

  @Override
  public void onMessage(ObjectRecord<String, ProductVo> message) {
    ProductVo vo = message.getValue();
    log.info("message.getId():{},  getValue:{}", message.getId().getValue(), vo);

    switch (applicationPort) {
      case 8081:
        // 模擬啥都沒做, , 會留在 redis 上
        log.info("do nothing");
        break;
      case 8082:
        // 模擬有 ack 但沒有把 message 刪掉, 會留在 redis 上
        redisStreamComponent.ack(message.getStream(), RedisStreamConfig.GROUP_NAME1, message.getId().getValue());
        log.info("ack");
        break;
      default:
        // 消息執行完畢，沒有報錯，ack確認
        redisStreamComponent.ack(message.getStream(), RedisStreamConfig.GROUP_NAME1, message.getId().getValue());
        // ack之後刪除消息
        redisStreamComponent.del(message.getStream(), message.getId().getValue());
        // 不會留在 redis 上
        log.info("ack & delete");
    }
    waiting(3);
  }


  private void waiting(int seconds) {
    try {
      Thread.sleep(seconds * 1000L);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
    }
  }
}
