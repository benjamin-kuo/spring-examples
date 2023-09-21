package com.bk.sample.service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;
import com.bk.sample.component.RedisStreamComponent;
import com.bk.sample.config.RedisStreamConfig;
import com.bk.sample.vo.ProductVo;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class RedisStreamService implements StreamListener<String, ObjectRecord<String, ProductVo>> {

  @Value("${server.port:8080}")
  int applicationPort;

  private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final RedisStreamComponent redisStreamComponent;

  @Override
  public void onMessage(ObjectRecord<String, ProductVo> message) {
    ProductVo vo = message.getValue();
    log.info("message.getId():{},  getValue:{}", message.getId().getValue(), vo);

    switch (applicationPort) {
      case 8080:
        // 模擬啥都沒做, , 會留在 redis 上
        log.info("do nothing");
        break;
      case 8082:
        // 模擬有 ack 但沒有把 message 刪掉, 會留在 redis 上
        redisStreamComponent.ack(message.getStream(), RedisStreamConfig.GROUP_NAME, message.getId().getValue());
        log.info("ack");
        break;
      default:
        // 消息執行完畢，沒有報錯，ack確認
        redisStreamComponent.ack(message.getStream(), RedisStreamConfig.GROUP_NAME, message.getId().getValue());
        // ack之後刪除消息
        redisStreamComponent.del(message.getStream(), message.getId().getValue());
        // 不會留在 redis 上
        log.info("ack & delete");
    }
    waiting(3);
  }

  // 原地處理, 可以執行到業務服務時
  public void doResetUnAck(){

    // 直接從消費者組隊列中查詢未確認消息
    Map<RecordId, PendingMessage> pendingMessageMap = redisStreamComponent
        .pending(RedisStreamConfig.STREAM_NAME, RedisStreamConfig.GROUP_NAME)
        .stream()
        // 當未確認消息時間超過 N 秒 (eg 10 秒) 才重新投遞消息，防止正在處理的消息被更新投遞
        .filter(e -> e.getElapsedTimeSinceLastDelivery().getSeconds() > 10)
        .collect(Collectors.toMap(PendingMessage::getId, Function.identity()));

    // 從隊列中讀取指定範圍消息 - 過濾 - 取出 pending 消息
    List<ObjectRecord<String, ProductVo>> pendingRecords = redisStreamComponent
        .range(RedisStreamConfig.STREAM_NAME, pendingMessageMap.keySet()).stream()
        // 只取 pending 消息
        .filter(e -> pendingMessageMap.containsKey(e.getId()))
        .collect(Collectors.toList());

    pendingRecords.forEach(message -> {

      // 消息執行完畢，沒有報錯，ack確認
      redisStreamComponent.ack(message.getStream(), RedisStreamConfig.GROUP_NAME, message.getId().getValue());
      // ack之後刪除消息
      redisStreamComponent.del(message.getStream(), message.getId().getValue());
      log.info("doResetUnAckByClaim ack & delete original message");
    });
  }

  // 沒有 ACK 的, 採重新投遞, 給原來的 consumer 群組 去處理
  public void doRestUnAckByAdd() {
    // 直接從消費者組隊列中查詢未確認消息
    Map<RecordId, PendingMessage> pendingMessageMap = redisStreamComponent
        .pending(RedisStreamConfig.STREAM_NAME, RedisStreamConfig.GROUP_NAME)
        .stream()
        // 當未確認消息時間超過 N 秒 (eg 10 秒) 才重新投遞消息，防止正在處理的消息被更新投遞
        .filter(e -> e.getElapsedTimeSinceLastDelivery().getSeconds() > 10)
        .collect(Collectors.toMap(PendingMessage::getId, Function.identity()));

    // 從隊列中讀取指定範圍消息 - 過濾 - 取出 pending 消息
    List<ObjectRecord<String, ProductVo>> pendingRecords = redisStreamComponent
        .range(RedisStreamConfig.STREAM_NAME, pendingMessageMap.keySet()).stream()
        // 只取 pending 消息
        .filter(e -> pendingMessageMap.containsKey(e.getId()))
        .collect(Collectors.toList());
    //
    pendingRecords.forEach(message -> {
      // 重新投遞消息, 給原來的服務去執行
      String add = redisStreamComponent.add(StreamRecords.newRecord()
          // 設置內容
          .ofObject(message.getValue())
          // 設置隊列
          .withStreamKey(RedisStreamConfig.STREAM_NAME));

      log.info("add:{}", add);
      // 原有消息ack確認
      redisStreamComponent.ack(RedisStreamConfig.STREAM_NAME, RedisStreamConfig.GROUP_NAME, message.getId().getValue());

      // 原的消息刪除
       redisStreamComponent.del(RedisStreamConfig.STREAM_NAME, message.getId().getValue());

      log.info("doRestUnAckByAdd ack & delete original message");

    });
  }

  private void waiting(int seconds) {
    try {
      Thread.sleep(seconds * 1000L);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
    }
  }
}

