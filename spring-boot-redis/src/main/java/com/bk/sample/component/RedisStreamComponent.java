package com.bk.sample.component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import com.bk.sample.vo.ProductVo;
import lombok.extern.slf4j.Slf4j;
import static java.time.Duration.ofSeconds;

@Slf4j
@Component
public class RedisStreamComponent {
  @Autowired
  private StringRedisTemplate stringRedisTemplate;

  /**
   * 創建消費者組
   *
   * @param key   stream key
   * @param group stream group
   * @return {@link String}
   */
  public String createGroup(String key, String group) {
    return stringRedisTemplate.opsForStream().createGroup(key, group);
  }

  /**
   * 從Queue中讀取指定範圍消息
   *
   * @param key       stream key
   * @param recordIds record id set
   * @return {@link List}<{@link ObjectRecord}<{@link String}, {@link ProductVo}>>
   */
  public List<ObjectRecord<String, ProductVo>> range(String key, Set<RecordId> recordIds) {
    if (recordIds.isEmpty()) {
      return Collections.emptyList();
    }
    // 消息id排序
    List<String> sortedMessageIds = recordIds.stream().map(RecordId::getValue)
        .sorted(Comparator.comparingLong(messageId -> Long.parseLong(messageId.split("-")[0])))
        .sorted(Comparator.comparingInt(messageId -> Integer.parseInt(messageId.split("-")[1])))
        .collect(Collectors.toList());

    // 消息範圍 閉區間
    Range<String> range = Range.closed(sortedMessageIds.get(0), sortedMessageIds.get(sortedMessageIds.size() - 1));
    return stringRedisTemplate.opsForStream().range(ProductVo.class, key, range);
  }

  /**
   * 確認已消費
   *
   * @param key       stream key
   * @param group     stream group
   * @param recordIds record ids
   * @return {@link Long}
   */
  public Long ack(String key, String group, String... recordIds) {
    return stringRedisTemplate.opsForStream().acknowledge(key, group, recordIds);
  }

  /**
   * 發送消息
   *
   * @param record record
   * @return {@link String}
   */
  public String add(Record record) {
    return stringRedisTemplate.opsForStream().add(record).getValue();
  }

  /**
   * 刪除消息，這里的刪除僅僅是設置了標志位，不影響消息總長度
   * 消息存儲在stream的節點下，刪除時僅對消息做刪除標記，當一個節點下的所有條目都被標記為刪除時，銷毀節點
   *
   * @param key       stream key
   * @param recordIds record ids
   * @return {@link Long}
   */
  public Long del(String key, String... recordIds) {
    return stringRedisTemplate.opsForStream().delete(key, recordIds);
  }

  /**
   * 查詢Queue中pending消息(已讀取ack未確認)
   *
   * @param key stream key
   * @param group stream group
   * @return {@link List}<{@link PendingMessage}>
   */
  public List<PendingMessage> pending(String key, String group) {
    PendingMessages pending = stringRedisTemplate.opsForStream().pending(key, group, Range.unbounded(), Long.MAX_VALUE);
    return pending.stream().collect(Collectors.toList());
  }

}
