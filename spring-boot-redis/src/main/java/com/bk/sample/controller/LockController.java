package com.bk.sample.controller;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.bk.sample.service.RedisLockAsyncService;
import com.bk.sample.service.RedisLockService;


@RestController
public class LockController {
  @Autowired
  RedisLockService redisLockService;

  @Autowired
  RedisLockAsyncService redisLockAsyncService;

  @PostMapping("redis-lock")
  public String redisLock(@RequestParam String value) {
    redisLockService.lock(value);
    return LocalDateTime.now().toString();
  }

  @PostMapping("redis-async-lock")
  public String redisAsyncLock(@RequestParam String value) {
    redisLockAsyncService.asyncLock(value);
    return LocalDateTime.now().toString();
  }

}
