package io.nebula.common.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class LockUtil {
    private static final Logger log = LoggerFactory.getLogger(LockUtil.class);
    private static RedissonClient redissonClient;

    public static void init(RedissonClient client) {
        redissonClient = client;
    }

    public interface LockMethod<T> {
        T run() throws Throwable;
    }

    public static <T> T tryLock(String lockKey, int expireSeconds, LockMethod<T> method) {
        RLock lock = null;
        try {
            lock = redissonClient.getLock(lockKey);
            boolean acquired = lock.tryLock(expireSeconds, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("Failed to acquire lock: {}", lockKey);
                return null;
            }
            return method.run();
        } catch (Throwable e) {
            log.error("Lock execution error: {}", lockKey, e);
            throw new RuntimeException(e);
        } finally {
            if (lock != null && lock.isHeldByCurrentThread() && lock.isLocked()) {
                lock.unlock();
            }
        }
    }

    public static <T> T lock(String lockKey, LockMethod<T> method) {
        RLock lock = null;
        try {
            lock = redissonClient.getLock(lockKey);
            lock.lock();
            return method.run();
        } catch (Throwable e) {
            log.error("Lock execution error: {}", lockKey, e);
            throw new RuntimeException(e);
        } finally {
            if (lock != null && lock.isHeldByCurrentThread() && lock.isLocked()) {
                lock.unlock();
            }
        }
    }
}
