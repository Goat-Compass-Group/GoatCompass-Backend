package com.goatcompass.identity.infrastructure.cache;

import com.goatcompass.identity.application.port.RedisPort;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisAdapter implements RedisPort {
    private final StringRedisTemplate redisTemplate;

    @Override
    public void saveWithTtl(String key, String value, long ttl, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, ttl, timeUnit);
    }

    @Override
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public long incrementWithTtl(String key, long ttl, TimeUnit timeUnit) {
        Long value = redisTemplate.opsForValue().increment(key);
        long current = value == null ? 1L : value;
        if (current == 1L) {
            redisTemplate.expire(key, ttl, timeUnit);
        }
        return current;
    }
}
