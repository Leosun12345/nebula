package io.nebula.common.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    private static final Logger log = LoggerFactory.getLogger(RedisUtil.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // ==================== 基础操作 ====================

    public void set(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Redis set error: key={}", key, e);
        }
    }

    public void setWithExpire(String key, String value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("Redis setWithExpire error: key={}", key, e);
        }
    }

    public String get(String key) {
        try {
            ValueOperations<String, String> ops = redisTemplate.opsForValue();
            return ops.get(key);
        } catch (Exception e) {
            log.error("Redis get error: key={}", key, e);
            return null;
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis delete error: key={}", key, e);
        }
    }

    public void expire(String key, long timeout) {
        try {
            redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis expire error: key={}", key, e);
        }
    }

    // ==================== Hash 操作 ====================

    public void hset(String key, String field, String value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
        } catch (Exception e) {
            log.error("Redis hset error: key={}, field={}", key, field, e);
        }
    }

    public String hget(String key, String field) {
        try {
            return redisTemplate.opsForHash().get(key, field).toString();
        } catch (Exception e) {
            log.error("Redis hget error: key={}, field={}", key, field, e);
            return null;
        }
    }

    public void hdel(String key, String... fields) {
        try {
            redisTemplate.opsForHash().delete(key, (String[]) fields);
        } catch (Exception e) {
            log.error("Redis hdel error: key={}", key, e);
        }
    }

    // ==================== Set 操作 ====================

    public void sadd(String key, String... values) {
        try {
            redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("Redis sadd error: key={}", key, e);
        }
    }

    public Set<String> smembers(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("Redis smembers error: key={}", key, e);
            return null;
        }
    }

    public void srem(String key, String... values) {
        try {
            redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            log.error("Redis srem error: key={}", key, e);
        }
    }

    // ==================== 🆕 Keys 操作 ====================

    /**
     * 获取匹配指定模式的所有 Key
     *
     * @param pattern 匹配模式，如 "room:node:*"
     * @return 匹配的 Key 集合
     */
    public Set<String> keys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            log.error("Redis keys error: pattern={}", pattern, e);
            return null;
        }
    }

    /**
     * 获取所有 Key (慎用，大数据量时性能差)
     */
    public Set<String> getAllKeys() {
        return keys("*");
    }

    /**
     * 批量删除匹配模式的 Key
     *
     * @param pattern 匹配模式
     * @return 删除的 Key 数量
     */
    public long deleteKeys(String pattern) {
        Set<String> keys = keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        try {
            return redisTemplate.delete(keys);
        } catch (Exception e) {
            log.error("Redis deleteKeys error: pattern={}", pattern, e);
            return 0;
        }
    }

    // ==================== 🆕 批量操作 ====================

    /**
     * 批量获取多个 Key 的值
     */
    public List<String> multiGet(Collection<String> keys) {
        try {
            return redisTemplate.opsForValue().multiGet(keys);
        } catch (Exception e) {
            log.error("Redis multiGet error", e);
            return null;
        }
    }

    /**
     * 检查 Key 是否存在
     */
    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Redis hasKey error: key={}", key, e);
            return false;
        }
    }

    /**
     * 获取 Key 的剩余生存时间 (秒)
     */
    public Long getExpire(String key) {
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis getExpire error: key={}", key, e);
            return -1L;
        }
    }
}
