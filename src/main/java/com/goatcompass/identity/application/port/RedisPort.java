package com.goatcompass.identity.application.port;

import java.util.concurrent.TimeUnit;

public interface RedisPort {
    void saveWithTtl(String key, String value, long ttl, TimeUnit timeUnit);

    boolean hasKey(String key);

    String getValue(String key);

    void deleteKey(String key);

    long incrementWithTtl(String key, long ttl, TimeUnit timeUnit);
}
