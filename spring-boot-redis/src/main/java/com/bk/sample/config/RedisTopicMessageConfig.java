package com.bk.sample.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import com.bk.sample.service.RedisTopicPubSubService;

@Component
public class RedisTopicMessageConfig {

  private final static Logger log = LoggerFactory.getLogger(RedisTopicMessageConfig.class);

  final public static String CUSTOM_TOPIC = "CUSTOM_TOPIC";
  final public static String CUSTOM_TOPIC_ACTION_KEY = "CUSTOM_TOPIC_ACTION_KEY";

  // Channel Topic
  @Bean
  public ChannelTopic customChannelTopic() {
    return new ChannelTopic(CUSTOM_TOPIC);
  }

  // 設定監聽
  @Bean
  RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
      RedisTopicPubSubService redisListener) {
    RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
    redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
    redisMessageListenerContainer.addMessageListener(redisListener, customChannelTopic());
    return redisMessageListenerContainer;
  }
}
