package com.bk.sample.service;


import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.bk.sample.util.RedisLock;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class RedisLockService {

  @Autowired
  StringRedisTemplate redisTemplate;

  @Autowired
  RedisLockAsyncService redisLockAsyncService;

  public void lock(String actionKey) {

    boolean locked = false;
    try {
      locked = RedisLock.tryLock(redisTemplate, actionKey, "Locked", Duration.ofSeconds(30000).toMillis());
      if (locked) {
        log.info("{}, get lock", actionKey);

        log.info("doing");

        waiting(5);

        log.info("done!");
      }else{
        log.info("{}, locked", actionKey);
      }
    } finally {
      if(locked) {
        RedisLock.unlock(redisTemplate, actionKey);
        // 移除 redis 上的 key
        redisTemplate.delete(actionKey);
        log.info("{}, unlock", actionKey);
      }
    }
  }

  public void asyncLock(String actionKey) {
    redisLockAsyncService.asyncLock(actionKey);
  }


  private void waiting(int seconds) {
    try {
      Thread.sleep(seconds * 1000L);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
    }
  }

}
