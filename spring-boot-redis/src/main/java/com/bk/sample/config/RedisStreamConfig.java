package com.bk.sample.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import com.bk.sample.component.RedisStreamComponent;
import com.bk.sample.service.RedisStreamBService;
import com.bk.sample.service.RedisStreamAService;
import com.bk.sample.vo.ProductVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisStreamConfig {

  @Value("${server.port:8080}")
  int applicationPort;

  //隊列名稱
  public final static String STREAM_NAME = "eg-stream";

  //消費者組名稱
  public final static String GROUP_NAME = "eg-group";

  //消費者組名稱
  public final static String GROUP_NAME1 = "eg-group1";

  //消費者名稱
  public final static String CONSUMER_NAME = "eg-consumer";

  @Autowired
  RedisStreamComponent redisStreamComponent;

  @Autowired
  private RedisStreamAService redisStreamAService;

  @Autowired
  private RedisStreamBService redisStreamBService;

  /**
   * 創建 Redis Stream 集群消費的容器（注冊一個消費者類作為多個消費者）
   */
  @Bean(initMethod = "start", destroyMethod = "stop")
  public StreamMessageListenerContainer<String, ObjectRecord<String, ProductVo>> redisStreamMessageListenerContainer(
      RedisConnectionFactory connectionFactory) {

    // 1. StreamMessageListenerContainer 容器
    // 2. options 配置
    StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, ProductVo>> containerOptions =
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
            .batchSize(10)                      // 一次性最多拉取多少條消息
            .targetType(ProductVo.class)        // 目標類型
            .build();

    // 3. container 對象
    StreamMessageListenerContainer<String, ObjectRecord<String, ProductVo>> container =
        StreamMessageListenerContainer.create(connectionFactory, containerOptions);

    try {
      // 4. 預設 Stream 的 key & 建立監聽群組
      redisStreamComponent.createGroup(STREAM_NAME, GROUP_NAME1);
      redisStreamComponent.createGroup(STREAM_NAME, GROUP_NAME);
    } catch (Exception ignore) {
      //ignore.printStackTrace();
    }

    // 5. 創建 Consumer 對象
    String consumerName = CONSUMER_NAME + "-" + applicationPort;


    Subscription subscription = container.receive(
        Consumer.from(GROUP_NAME, consumerName),
        StreamOffset.create(STREAM_NAME, ReadOffset.lastConsumed()),
        redisStreamAService
    );
    log.info("[RedisStreamConfig][StreamKey({}) 對應的 Listener ({})]", STREAM_NAME, redisStreamAService.getClass().getSimpleName());

    // 發一個訊息, 給不同的 消費者 同時處理, 類似 pub/sub
    Subscription subscription1 = container.receive(
        Consumer.from(GROUP_NAME1, consumerName),
        StreamOffset.create(STREAM_NAME, ReadOffset.lastConsumed()),
        redisStreamBService
    );
    log.info("[RedisStreamConfig][StreamKey({}) 對應的 Listener ({})]", STREAM_NAME, redisStreamBService.getClass().getSimpleName());

    return container;
  }
}
