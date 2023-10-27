package com.bk.sample.service;


import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import com.bk.sample.config.RedisTopicMessageConfig;

@Service
public class RedisTopicPubSubService implements MessageListener {

  private static final Logger log = LoggerFactory.getLogger(RedisTopicPubSubService.class);

  @Autowired
  StringRedisTemplate stringRedisTemplate;

  @Autowired
  private ChannelTopic customChannelTopic;

  @Autowired
  private ChannelTopic customChannelTopic1;

  // 收訊息, 所有 listener 的都會收到訊息, 若要只執行一次, 則要額外做 lock
  @Override
  public void onMessage(Message message, byte[] pattern) {
    String actionKey = new String(message.getBody(), StandardCharsets.UTF_8);
    String topic = Objects.isNull(pattern) ? "" : new String(pattern, StandardCharsets.UTF_8);
    log.info("actionKey:{}, topic:{}, value:{}", actionKey, topic,
        stringRedisTemplate.opsForValue().get(RedisTopicMessageConfig.CUSTOM_TOPIC_ACTION_KEY));
    waiting(10);
    stringRedisTemplate.delete(RedisTopicMessageConfig.CUSTOM_TOPIC_ACTION_KEY);
    log.info("actionKey:{} deleted", actionKey);
  }

  // 推廣播訊息
  public String pubMessage(String value) {
    //stringRedisTemplate.boundHashOps(RedisMessageListener.CUSTOM_TOPIC_ACTIONKEY).put("KEY-A", "VAULE-AAA");
    stringRedisTemplate.boundValueOps(RedisTopicMessageConfig.CUSTOM_TOPIC_ACTION_KEY).set(value);

    if("0".equals(value)){
      stringRedisTemplate.convertAndSend(customChannelTopic.getTopic(), RedisTopicMessageConfig.CUSTOM_TOPIC_ACTION_KEY);
    }else{
      stringRedisTemplate.convertAndSend(customChannelTopic1.getTopic(), RedisTopicMessageConfig.CUSTOM_TOPIC_ACTION_KEY);
    }

    return value;
  }

  private void waiting(int seconds) {
    try {
      Thread.sleep(seconds * 1000L);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
    }
  }
}
