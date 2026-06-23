package io.nebula.common.redis;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis 队列工具类
 * 基于 Redis List 实现的消息队列
 *
 * @author leo
 * @since 1.0.0
 */
@Component
public class QueueUtil {
    private static final Logger log = LoggerFactory.getLogger(QueueUtil.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ==================== 生产者 ====================

    /**
     * 向队列左侧推入单个消息
     */
    public void putQueue(String queue, Object... objs) {
        if (objs == null || objs.length == 0) {
            return;
        }

        String[] strings = new String[objs.length];
        for (int i = 0; i < objs.length; i++) {
            Object obj = objs[i];
            if (obj instanceof String) {
                strings[i] = (String) obj;
            } else {
                strings[i] = JSONObject.toJSONString(obj);
            }
        }

        try {
            redisTemplate.opsForList().leftPushAll(queue, strings);
            log.debug("Pushed {} messages to queue: {}", strings.length, queue);
        } catch (Exception e) {
            log.error("Failed to push to queue: {}", queue, e);
        }
    }

    /**
     * 向队列左侧推入集合
     */
    public void putQueue(String queue, List<?> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        putQueue(queue, list.toArray());
    }

    /**
     * 向队列右侧推入单个消息
     */
    public void putQueueRight(String queue, Object obj) {
        String str = obj instanceof String ? (String) obj : JSONObject.toJSONString(obj);
        try {
            redisTemplate.opsForList().rightPush(queue, str);
            log.debug("Pushed to queue right: {}", queue);
        } catch (Exception e) {
            log.error("Failed to rightPush to queue: {}", queue, e);
        }
    }

    // ==================== 消费者 ====================

    /**
     * 从队列右侧拉取单个消息 (非阻塞)
     */
    public String pull(String queue) {
        try {
            Object obj = redisTemplate.opsForList().rightPop(queue);
            return obj != null ? obj.toString() : null;
        } catch (Exception e) {
            log.error("Failed to pull from queue: {}", queue, e);
            return null;
        }
    }

    /**
     * 从队列右侧拉取批量消息 (非阻塞)
     */
    public List<String> pullBatch(String queue, int size) {
        List<String> result = new ArrayList<>();
        try {
            List<Object> objects = redisTemplate.opsForList().rightPop(queue, size);
            if (objects != null) {
                for (Object obj : objects) {
                    if (obj != null) {
                        result.add(obj.toString());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to pullBatch from queue: {}", queue, e);
        }
        return result;
    }

    /**
     * 从队列右侧拉取单个消息 (阻塞)
     */
    public String pullBlocking(String queue, long timeout, TimeUnit unit) {
        try {
            Object obj = redisTemplate.opsForList().rightPop(queue, timeout, unit);
            return obj != null ? obj.toString() : null;
        } catch (Exception e) {
            log.error("Failed to pullBlocking from queue: {}", queue, e);
            return null;
        }
    }

    /**
     * 从队列右侧拉取批量消息 (阻塞)
     */
    public List<String> pullBlockingBatch(String queue, int size, long timeout, TimeUnit unit) {
        List<String> result = new ArrayList<>();
        try {
            for (int i = 0; i < size; i++) {
                Object obj = redisTemplate.opsForList().rightPop(queue, timeout, unit);
                if (obj == null) {
                    break;
                }
                result.add(obj.toString());
            }
        } catch (Exception e) {
            log.error("Failed to pullBlockingBatch from queue: {}", queue, e);
        }
        return result;
    }

    // ==================== 队列管理 ====================

    /**
     * 获取队列长度
     */
    public long getQueueSize(String queue) {
        try {
            Long size = redisTemplate.opsForList().size(queue);
            return size != null ? size : 0;
        } catch (Exception e) {
            log.error("Failed to get queue size: {}", queue, e);
            return 0;
        }
    }

    /**
     * 获取队列中指定范围的消息 (不删除)
     */
    public List<Object> getQueueRange(String queue, long start, long end) {
        try {
            return redisTemplate.opsForList().range(queue, start, end);
        } catch (Exception e) {
            log.error("Failed to get queue range: {}", queue, e);
            return new ArrayList<>();
        }
    }

    /**
     * 删除整个队列
     */
    public void deleteQueue(String queue) {
        try {
            redisTemplate.delete(queue);
            log.debug("Deleted queue: {}", queue);
        } catch (Exception e) {
            log.error("Failed to delete queue: {}", queue, e);
        }
    }

    /**
     * 修剪队列 (只保留指定范围内的元素)
     */
    public void trimQueue(String queue, long start, long end) {
        try {
            redisTemplate.opsForList().trim(queue, start, end);
            log.debug("Trimmed queue: {} to range [{}, {}]", queue, start, end);
        } catch (Exception e) {
            log.error("Failed to trim queue: {}", queue, e);
        }
    }

    // ==================== 消息封装 ====================

    /**
     * 封装跨服务消息
     */
    public static String wrapMessage(String api, Object data) {
        JSONObject wrapper = new JSONObject();
        wrapper.put("api", api);
        wrapper.put("data", data);
        wrapper.put("timestamp", System.currentTimeMillis());
        return wrapper.toJSONString();
    }

    /**
     * 解析跨服务消息
     */
    public static JSONObject unwrapMessage(String message) {
        try {
            return JSONObject.parseObject(message);
        } catch (Exception e) {
            log.error("Failed to unwrap message: {}", message, e);
            return null;
        }
    }
}
