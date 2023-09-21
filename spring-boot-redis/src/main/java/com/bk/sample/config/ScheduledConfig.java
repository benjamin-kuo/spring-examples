package com.bk.sample.config;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ScheduledConfig {
  private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Bean
  public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
    //log.info("建立定時任務排程執行緒池 start");
    ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setPoolSize(10);
    threadPoolTaskScheduler.setThreadNamePrefix("tasker-");
    threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
    threadPoolTaskScheduler.setAwaitTerminationSeconds(60);
    //log.info("建立定時任務排程執行緒池 end");
    return threadPoolTaskScheduler;
  }
}
