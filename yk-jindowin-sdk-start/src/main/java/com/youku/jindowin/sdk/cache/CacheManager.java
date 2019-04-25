package com.youku.jindowin.sdk.cache;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * @author 吴聪帅
 * @Description
 * @Date : 下午8:05 2019/4/24 Modifyby:
 **/
@Component
public class CacheManager implements CacheService, CommandLineRunner {

    private static Logger logger = LoggerFactory.getLogger(CacheManager.class);

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    @Qualifier(value = "redisCacheManager")
    InnerCacheService redisCacheManager;

    @Autowired
    @Qualifier(value = "localCacheManager")
    InnerCacheService localCacheManager;

    private Boolean usingRedis;

    @Override
    public <T> T get(String key) {
        return get(key,null);
    }

    @Override
    public <T> T get(String key, Callable<T> valueLoader) {
        T result = null;
        if (valueLoader != null) {
            result = localCacheManager.get(key, () -> new CacheValue<>(valueLoader.call(), null));
        } else {
            result = localCacheManager.get(key, null);
        }
        if (result == null) {
            if (usingRedis) {
              return  redisCacheManager.get(
                    key, () -> new CacheValue<>(valueLoader.call(), null)
                );
            }
        }
        return result;
    }

    @Override
    public <K, V> Map<K, V> mget(List<K> keys, Callable<Map<K, V>> valueLoader) {
        Map<K, V> resultMap = localCacheManager.mget(keys,valueLoader );
        if (MapUtils.isEmpty(resultMap)) {
            if (usingRedis) {
                return redisCacheManager.mget(keys,valueLoader);
            }
        }
        return resultMap;
    }

    @Override
    public boolean delete(String key) {
        boolean result = localCacheManager.delete(key);
        if (usingRedis) {
            CompletableFuture.runAsync(() -> {
                redisCacheManager.delete(key);
            });
        }
        return result;
    }

    @Override
    public <T> boolean put(String key, T value) {
        boolean result = localCacheManager.put(key,new CacheValue<>(value,null));
        if (usingRedis) {
            CompletableFuture.runAsync(() -> {
                System.out.println("123");
                redisCacheManager.put(key,new CacheValue<>(value,null));
            });
        }
        return result;
    }

    @Override
    public <T> boolean put(String key, T value, long expreTimes) {
        boolean result = localCacheManager.put(key,new CacheValue<>(value,expreTimes));
        if (usingRedis) {
            CompletableFuture.runAsync(() -> {
                redisCacheManager.put(key,new CacheValue<>(value,expreTimes));
            });
        }
        return result;
    }

    @Override
    public boolean useRedis() {
        return this.usingRedis;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            redisTemplate.getConnectionFactory().getConnection();
            logger.info("redis is in using");
            usingRedis = true;
        } catch (Exception e) {
            usingRedis = false;
            logger.info("redis is not use");
        }
    }
}
