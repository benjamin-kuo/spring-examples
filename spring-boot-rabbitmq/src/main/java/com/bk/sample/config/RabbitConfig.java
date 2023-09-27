package com.bk.sample.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitConfig {

  // Direct 一個 Producer 負責發送 message 到 Queue 裡、而也只有一個 Consumer 去 Queue 裡消費 message
  public static final String QUEUE_NAME = "Sample.Queue";

  // 透過 Exchange 調控
  public static final String EXCHANGE_NAME = "Sample.Exchange";
  public static final String QUEUE_A = "Queue.A";
  public static final String QUEUE_B = "Queue.B";
  public static final String ROUTING_A = "Routing.A";
  public static final String ROUTING_B = "Routing.B";

  // FANOUT
  public static final String ROUTING_AB = "RoutingAB";

  // TOPIC
  public static final String EXCHANGE_TOPIC_NAME = "Sample.TOPIC.Exchange";
  public static final String QUEUE_Topic = "Queue.Topic";
  public static final String ROUTING_WITH = "Routing.*";

  // HEADER
  public static final String EXCHANGE_HEADER_NAME = "Sample.HEADER.Exchange";
  public static final String EXCHANGE_HEADER_KEY = "productId";
  public static final String QUEUE_HEADER_A = "Queue.Header.A";
  public static final String QUEUE_HEADER_B = "Queue.Header.B";

  // Retry
  public static final String QUEUE_RETRY = "Queue.Retry";

  // Manual Queue
  public static final String QUEUE_MANUEL_NAME = "Sample.Queue.Manual";

  @Bean
  Queue queue() {
    // durable：　RabbitMQ 重啟後　保留　or 移除 queue
    return new Queue(QUEUE_NAME, true);
  }

  @Bean
  Queue queueA() {
    return new Queue(QUEUE_A, true);
  }

  @Bean
  Queue queueB() {
    return new Queue(QUEUE_B, true);
  }

  @Bean
  Queue queueHeaderA() {
    return new Queue(QUEUE_HEADER_A, true);
  }

  @Bean
  Queue queueHeaderB() {
    return new Queue(QUEUE_HEADER_B, true);
  }

  @Bean
  DirectExchange exchange() {
    return new DirectExchange(EXCHANGE_NAME);
  }

  @Bean
  Queue queueTopic() {
    return new Queue(QUEUE_Topic, true);
  }

  @Bean
  Queue queueRetry() {
    return new Queue(QUEUE_RETRY, true);
  }

  @Bean
  Queue queueManual() {
    // yml, acknowledge-mode: manual
    return new Queue(QUEUE_MANUEL_NAME, true);
  }

  @Bean
  TopicExchange topicExchange() {
    return new TopicExchange(EXCHANGE_TOPIC_NAME);
  }

  @Bean
  HeadersExchange headerExchange() {
    return new HeadersExchange(EXCHANGE_HEADER_NAME);
  }

  // Exchange direct
  @Bean
  Binding bindingDirectToA(Queue queueA, DirectExchange exchange) {
    return BindingBuilder.bind(queueA).to(exchange).with(ROUTING_A);
  }

  // Exchange direct
  @Bean
  Binding bindingDirectToB(Queue queueB, DirectExchange exchange) {
    return BindingBuilder.bind(queueB).to(exchange).with(ROUTING_B);
  }

  // Exchange Fanout - AB
  @Bean
  Binding bindingFanoutA(Queue queueA, DirectExchange exchange) {
    return BindingBuilder.bind(queueA).to(exchange).with(ROUTING_AB);
  }

  // Exchange Fanout - AB
  @Bean
  Binding bindingFanoutB(Queue queueB, DirectExchange exchange) {
    return BindingBuilder.bind(queueB).to(exchange).with(ROUTING_AB);
  }

  // Exchange Topic routing - A
  @Bean
  Binding bindingTopicA(Queue queueA, TopicExchange topicExchange) {
    return BindingBuilder.bind(queueA).to(topicExchange).with(ROUTING_A);
  }

  // Exchange Topic routing - B
  @Bean
  Binding bindingTopicB(Queue queueB, TopicExchange topicExchange) {
    return BindingBuilder.bind(queueB).to(topicExchange).with(ROUTING_B);
  }

  @Bean
  Binding bindingTopic(Queue queueTopic, TopicExchange topicExchange) {
    return BindingBuilder.bind(queueTopic).to(topicExchange).with(ROUTING_WITH);
  }

  // Exchange Header
  @Bean
  Binding bindingHeaderA(Queue queueHeaderA, HeadersExchange headerExchange) {
    return BindingBuilder.bind(queueHeaderA).to(headerExchange).where(EXCHANGE_HEADER_KEY).matches("A");
  }

  @Bean
  Binding bindingHeaderB(Queue queueHeaderB, HeadersExchange headerExchange) {
    return BindingBuilder.bind(queueHeaderB).to(headerExchange).where(EXCHANGE_HEADER_KEY).matches("B");
  }

  public static void waiting(int seconds) {
    try {
      Thread.sleep(seconds * 1000L);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
    }
  }
}

