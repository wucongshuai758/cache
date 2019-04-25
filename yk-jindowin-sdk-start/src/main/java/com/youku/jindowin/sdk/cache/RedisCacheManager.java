package com.youku.jindowin.sdk.cache;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * @author 吴聪帅
 * @Description
 * @Date : 下午4:26 2019/4/24 Modifyby:
 **/
@Service
public class RedisCacheManager implements InnerCacheService {
    private static Logger logger = LoggerFactory.getLogger(RedisCacheManager.class);

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Override
    public <T> T get(String key, Callable<CacheValue<T>> valueLoader) {
        T value = (T)redisTemplate.opsForValue().get(key);
        if (value != null) {
            return value;
        }
        if (valueLoader != null) {
            try {
                CacheValue<T> result = valueLoader.call();
                if (result != null) {
                    put(key, result);
                }
                return result.getData();
            } catch (Exception e) {
                logger.error("redis cache reload exception key:" + key, e);
            }

        }
        return null;
    }

    @Override
    public <K, V> Map<K, V> mget(List<K> keys, Callable<Map<K, V>> valueLoader) {
        if (CollectionUtils.isEmpty(keys)) {
            return null;
        }
        Map<K, V> resultMap = new HashMap<>();
        for (K key : keys) {
            Object t = redisTemplate.opsForValue().get(key);
            if (t != null) {
                V value = (V)t;
                resultMap.put(key, value);
            }
        }
        if (resultMap.size() != keys.size()) {
            if (valueLoader != null) {
                try {
                    Map<K, V> data = valueLoader.call();
                    if (MapUtils.isNotEmpty(data)) {
                        data.forEach((k, v) -> {
                            if (!resultMap.containsKey(k)) {
                                resultMap.put(k, v);
                            }
                        });
                        CompletableFuture.runAsync(() -> {
                            data.forEach((k, v) -> {
                                redisTemplate.opsForValue().set((String)k, v);
                            });
                        });
                        return data;
                    }
                } catch (Exception e) {
                    logger.info("redis mget reload exception", e);
                }
            }
        }
        return resultMap;
    }

    @Override
    public boolean delete(String key) {
        if (key == null) {
            return false;
        }
        return redisTemplate.delete(key);
    }

    @Override
    public <T> boolean put(String key, CacheValue<T> value) {
        if (value == null || key == null) {
            return false;
        }
        if (value.getExpireTime() != null) {
            redisTemplate.opsForValue().set(key, value.getData(), value.getExpireTime());
        } else {
            redisTemplate.opsForValue().set(key, value.getData());
        }
        return true;
    }
}
