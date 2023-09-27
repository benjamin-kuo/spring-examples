package com.bk.sample.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.bk.sample.component.RedisStreamComponent;
import com.bk.sample.config.RedisStreamConfig;
import com.bk.sample.service.RedisListPushPopService;
import com.bk.sample.service.RedisStreamAService;
import com.bk.sample.service.RedisTopicPubSubService;
import com.bk.sample.vo.ProductVo;


@RestController
public class SampleController {

  @Value("${spring.application.name:}")
  String applicationName;

  @Autowired
  RedisListPushPopService redisListService;

  @Autowired
  RedisTopicPubSubService redisTopicPubSubService;

  @Autowired
  RedisStreamComponent redisStreamComponent;

  @Autowired
  RedisStreamAService redisStreamService;

  @GetMapping("")
  public String hello() {
    return "Hello " + applicationName;
  }

  @PostMapping("redis-list-push")
  public long redisListPush(@RequestParam String value) {
    return redisListService.push(value);
  }

  @GetMapping("redis-list-pop")
  public String redisListPop() {
    return redisListService.pop();
  }

  @PostMapping("redis-topic-pub")
  public String redisTopicPub(@RequestParam String value) {
    return redisTopicPubSubService.pubMessage(value);
  }

  @GetMapping("/redis-stream/{count}")
  public String addMore(@PathVariable("count") Integer count) {
    for (int i = 0; i < count; i++) {
      redisStreamComponent.add(StreamRecords.newRecord()
          .ofObject(new ProductVo("ID-" + i, i))
          .withStreamKey(RedisStreamConfig.STREAM_NAME));
    }
    return "發送 " + count + " 筆成功";
  }

  @GetMapping("/redis-stream-do-rest-by-add")
  public String doRestMessageByAdd() {
    redisStreamService.doRestUnAckByAdd();
    return "Add OK";
  }

  @GetMapping("/redis-stream-do-rest")
  public String doRestMessage() {
    redisStreamService.doResetUnAck();
    return "Do rest OK";
  }

}
