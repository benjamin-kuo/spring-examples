package com.bk.sample.service;


import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisListPushPopService {

  @Autowired(required = false)
  StringRedisTemplate stringRedisTemplate;

  private final String LIST_KEY = "RedisList";

  // 推進 List
  public Long push(String value) {
    return stringRedisTemplate.opsForList().leftPush(LIST_KEY, value);
  }

  // 取出 List
  public String pop() {
    if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(LIST_KEY))) {
      if (stringRedisTemplate.opsForList().size(LIST_KEY) != null) {
        return Objects.requireNonNull(stringRedisTemplate.opsForList().leftPop(LIST_KEY));
      }
    }
    return null;
  }
}
