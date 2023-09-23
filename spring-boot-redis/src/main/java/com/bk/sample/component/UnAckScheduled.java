package com.bk.sample.component;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.bk.sample.service.RedisStreamService;
import com.bk.sample.util.RedisLock;

@Component
public class UnAckScheduled {
    private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Value("${server.port:8080}")
    int applicationPort;

    @Autowired(required = false)
    RedisTemplate redisTemplate;

    @Autowired
    RedisStreamService redisStreamService;

    private final String LOCK_KEY = "DO-ACK";

    private static final String CRON_EVERY_30_SECONDS = "*/20 * * * * *";

    //@Scheduled(cron = CRON_EVERY_30_SECONDS)
    public void scheduled() {
        if(RedisLock.tryLock(redisTemplate, LOCK_KEY, "LOCK", 60L)){
            log.info("scheduled in {}", applicationPort);

            // 重新處理任務
            redisStreamService.doResetUnAck();

            waiting(15);
            RedisLock.unlock(redisTemplate, LOCK_KEY);
        }else{
            log.info("scheduled {}: 取不到 lock", applicationPort);
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

