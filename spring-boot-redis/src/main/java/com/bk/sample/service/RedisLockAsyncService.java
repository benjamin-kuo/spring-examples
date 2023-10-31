package com.bk.sample.service;


import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.bk.sample.util.RedisLock;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class RedisLockAsyncService {

  @Autowired
  StringRedisTemplate stringRedisTemplate;

  @Autowired
  RedisTemplate redisTemplate;

  private RedisLockAsyncService redisLockAsyncService;

  // 避免循環依賴
  @Autowired
  public RedisLockAsyncService(@Lazy RedisLockAsyncService redisLockAsyncService) {
    this.redisLockAsyncService = redisLockAsyncService;
  }



  @Async("asyncConfigBean")
  public void asyncLock(String actionKey) {

    String updateNtfNotifyOperationLastCall = "updateNtfNotifyOperationLastCall";       //最後一次呼叫的時間
    String updateNtfNotifyOperationStart = "updateNtfNotifyOperationStart";             //上次執行的啓動時間
    String updateNtfNotifyOperationEnd = "updateNtfNotifyOperationEnd";                 //上次執行的結束時間

    boolean locked = false;
    boolean unlock = false;
    LocalDateTime now = LocalDateTime.now();
    try {
      redisTemplate.opsForValue().set(updateNtfNotifyOperationLastCall, now);
      locked = RedisLock.tryLock(stringRedisTemplate, actionKey, "Locked", 10);
      if (locked) {
        log.info("{}, get lock:{}", actionKey,locked);
        boolean doUpdate = false;   // 要不要跑更新

        Boolean hasStartKey = redisTemplate.hasKey(updateNtfNotifyOperationStart);
        log.info("hasStartKey:{}", hasStartKey);


        LocalDateTime ntfNotifyOperationStart = null;
        LocalDateTime ntfNotifyOperationEnd = null;
        LocalDateTime ntfNotifyOperationLastCall = null;

        // 從沒執行過
        if (redisTemplate.hasKey(updateNtfNotifyOperationStart) == null) {
          doUpdate = true;
          redisTemplate.opsForValue().set(updateNtfNotifyOperationStart, now);
          log.info("AAA");
        }else{
          Object ntfNotifyOperationStartObj = redisTemplate.opsForValue().get(updateNtfNotifyOperationStart);
          if (ntfNotifyOperationStartObj != null) {
            ntfNotifyOperationStart = (LocalDateTime) ntfNotifyOperationStartObj;
          }else{
            redisTemplate.opsForValue().set(updateNtfNotifyOperationStart, now);
            ntfNotifyOperationStart = now;
            doUpdate = true;
            log.info("AAA-AAA");
          }
        }

        log.info("doUpdate:{}", doUpdate);

        Boolean hasEndKey = redisTemplate.hasKey(updateNtfNotifyOperationEnd);
        // 上次有結束
        if (!doUpdate && hasEndKey != null && hasEndKey.equals(Boolean.TRUE)) {
          Object ntfNotifyOperationEndObj = redisTemplate.opsForValue().get(updateNtfNotifyOperationEnd);
          if (ntfNotifyOperationEndObj != null) {
            ntfNotifyOperationEnd = (LocalDateTime) ntfNotifyOperationEndObj;
          }
          // 再判斷 endTime = startTime, 正常的結束
          if (ntfNotifyOperationEnd != null && ntfNotifyOperationEnd.compareTo(ntfNotifyOperationStart) == 0) {
            doUpdate = true;
            log.info("BBB");
          }
        }

        // 如果這次 call 的時間, 比上次執行時間超過 20 分鐘, 強制執行一次
        long diffTime = (now.toEpochSecond(ZoneOffset.of("+08:00")) - ntfNotifyOperationStart.toEpochSecond(ZoneOffset.of("+08:00")));
        log.info("{} - now", now);
        log.info("{} - start", ntfNotifyOperationStart);
        log.info("diffTime:{}", diffTime);
        if(diffTime > 1200){
          doUpdate = true;
          log.info("CCC");
        }

        if (doUpdate) {
          redisTemplate.opsForValue().set(updateNtfNotifyOperationStart, now);
          log.info("doing");

          waiting(5);

          unlock = true;
          redisTemplate.opsForValue().set(updateNtfNotifyOperationEnd, now);
          log.info("done!");
          // 檢查是不是有在期間呼叫
          Object updateNtfNotifyOperationLastCallObj = redisTemplate.opsForValue().get(updateNtfNotifyOperationLastCall);
          if (updateNtfNotifyOperationLastCallObj != null) {
            ntfNotifyOperationLastCall = (LocalDateTime) updateNtfNotifyOperationLastCallObj;
            log.info("{} - now", now);
            log.info("{} - start", ntfNotifyOperationStart);
            log.info("{} - lastCall", ntfNotifyOperationLastCall);
            if(ntfNotifyOperationLastCall.isAfter(now)){
              log.info("期間有再呼叫....{}", ntfNotifyOperationLastCall);
              redisLockAsyncService.asyncLock(actionKey);
            }
          }
        }

      } else {
        log.info("{}, locked", now);
      }
    } finally {
      if (unlock) {
        RedisLock.unlock(stringRedisTemplate, actionKey);
        log.info("{}, unlock", actionKey);
      }
    }
  }






  private void waiting(int seconds) {
    try {
      Thread.sleep(seconds * 1000L);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
    }
  }

}
