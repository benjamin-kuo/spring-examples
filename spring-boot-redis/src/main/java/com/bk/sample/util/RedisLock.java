package com.bk.sample.util;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

public class RedisLock {
  private static final Long SUCCESS = 1L;

  /**
   * 加鎖
   */
  public static boolean tryLock(RedisTemplate redisTemplate, String key, String value, long expireSeconds) {
    try {
      //SET命令返回OK ，則證明獲取鎖成功
      return redisTemplate.opsForValue().setIfAbsent(key, value, expireSeconds, TimeUnit.SECONDS);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * 解鎖
   */
  public static boolean unlock(RedisTemplate redisTemplate, String key) {
    try {
      Long result = redisTemplate.delete(Collections.singletonList(key));
      if(result.compareTo(SUCCESS) == 0){
        return true;
      }else {
        return false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
