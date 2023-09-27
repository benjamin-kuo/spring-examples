package com.bk.sample.controller;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.bk.sample.service.RabbitMQConsumer;
import com.bk.sample.service.RabbitMQSender;
import com.bk.sample.vo.ProductVo;

@RestController
public class SampleController {

  @Autowired
  RabbitMQSender rabbitMQSender;

  @Autowired
  RabbitMQConsumer rabbitMQConsumer;

  @GetMapping("sample")
  public String sampleGet() {
    return "hello";
  }

  @GetMapping("/send/{count}")
  public String addMore(@PathVariable("count") Integer count) {
    for (int i = 0; i < count; i++) {
      rabbitMQSender.send(new ProductVo("ID-" + i, i));
    }
    return "發送 " + count + " 筆成功";
  }

  @GetMapping("exchange-direct/{routing}")
  public String sendExchangeDirect(@PathVariable("routing") String routing) {
    LocalDateTime now = LocalDateTime.now();
    rabbitMQSender.exchangeDirect(routing, new ProductVo(now.toString(), now.getSecond()));
    return "發送 Direct " + routing + "成功";
  }

  @GetMapping("exchange-fanout")
  public String sendExchangeFanout() {
    LocalDateTime now = LocalDateTime.now();
    rabbitMQSender.exchangeFanout(new ProductVo(now.toString(), now.getSecond()));
    return "發送 withh Fanout 成功";
  }

  @GetMapping("exchange-topic/{routing}")
  public String sendExchangeTopic(@PathVariable("routing") String routing) {
    LocalDateTime now = LocalDateTime.now();
    rabbitMQSender.exchangeTopic(routing, new ProductVo(now.toString(), now.getSecond()));
    return "發送 with Topic with " + routing + " 成功";
  }

  @GetMapping("exchange-header/{header}")
  public String sendExchangeHeader(@PathVariable("header") String header) {
    LocalDateTime now = LocalDateTime.now();
    rabbitMQSender.exchangeHeader(header, now.toString());
    return "發送 with Header " + header + " 成功";
  }

  @GetMapping("/send-retry")
  public String sendRetry() {
    LocalDateTime now = LocalDateTime.now();
    rabbitMQSender.sendRetry(new ProductVo(now.toString(), now.getSecond()));
    return "發送 Retry 成功";
  }

  @GetMapping("/receive-retry")
  public String receiveRetry() {
    rabbitMQConsumer.receiveRetry();
    return "Do Retry 成功";
  }

  //0.模擬失敗, 1. 模擬成功, 2.模擬不自動 ACK(要調整 yml 的設定)
  @GetMapping("/send-manual/{type}")
  public String sendManual(@PathVariable("type") Integer type) {
    rabbitMQSender.sendManual(new ProductVo("ID-" + type, type));
    return "發送 " + type + " 成功";
  }

}
