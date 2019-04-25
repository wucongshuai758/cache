package com.youku.jindowin.sdk.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 吴聪帅
 * @Description 本地缓存
 * @Date : 下午7:30 2019/4/23 Modifyby:
 **/
@Component
public class LocalCacheManager implements InnerCacheService {
    private static Logger logger = LoggerFactory.getLogger(LocalCacheManager.class);

    /**
     * 缓存key个数上限
     */
    @Value("${localcache.maxsize:50000}")
    private int cacheMaxSize;

    /**
     * 缓存数据
     */
    private static Cache<String, CacheValue<?>> cache = null;

    private static Cache<String, Lock> lockMap = CacheBuilder.newBuilder().recordStats().maximumSize(5000)
        .build();

    @PostConstruct
    public void init() {
        if (cache == null) {
            cache = CacheBuilder.newBuilder().recordStats().maximumSize(this.getCacheMaxSize())
                .build();
        }
    }

    @Override
    public <T> T get(String key, Callable<CacheValue<T>> valueLoader) {
        if (key == null) {
            return null;
        }
        CacheValue<T> result = null;
        try {
            if (valueLoader != null ) {
                result = (CacheValue<T>)cache.get(key, valueLoader);
            } else {
                Object temp = cache.getIfPresent(key);
                if (temp != null) {
                    result = (CacheValue<T>)temp;
                }
            }
        } catch (Exception e) {
            logger.error("local cache get exception key:" + key, e);
        }

        if (result != null) {
            boolean locked = false;
            Lock lockHandle = null;
            try {

                if (CacheValue.isExpire(result)) {
                    if (valueLoader != null) {
                        lockHandle = getLock(key);
                        locked = lockHandle.tryLock();
                        if (locked) {
                            result = valueLoader.call();
                            cache.put(key, result);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("reload localcache error key :" + key, e);
            } finally {
                if (locked && lockHandle != null) {
                    lockHandle.unlock();
                }
            }

        }
        return result == null ? null : result.getData();
    }

    @Override
    public <K, V> Map<K, V> mget(List<K> keys, Callable<Map<K, V>> valueLoader) {
        Map<K, V> resultMap = Maps.newHashMap();
        if (CollectionUtils.isEmpty(keys)) {
            return null;
        }
        Iterator<K> it = keys.iterator();
        int count = 0;
        while (it.hasNext()) {
            K key = it.next();
            try {
                CacheValue<V> cacheValue = null;
                Object temp = cache.getIfPresent(key);
                if (temp != null) {
                    cacheValue = (CacheValue<V>)temp;
                }
                if (cacheValue != null && !CacheValue.isExpire(cacheValue)) {
                    if (cacheValue.getData() != null) {
                        resultMap.put(key, cacheValue.getData());
                    }
                    count++;
                }
            } catch (Exception e) {
                logger.error("localcache mget exception key:" + key, e);
            }
        }
        if (count == keys.size()) {
            return resultMap;
        }
        try {
            if (valueLoader != null) {
                Map<K, V> data = valueLoader.call();
                if (MapUtils.isNotEmpty(data)) {
                    keys.forEach(key -> {
                        if (data.containsKey(key) && !resultMap.containsKey(key)) {
                            cache.put((String)key, new CacheValue<>(data.get(key), null));
                            resultMap.put(key, data.get(key));
                        }
                    });
                }
            }
        } catch (Exception e) {
            logger.error("localcache mget local exception", e);
        }
        return resultMap;
    }

    @Override
    public boolean delete(String key) {
        if (key == null) {
            return false;
        }
        cache.invalidate(key);
        return true;
    }

    @Override
    public <T> boolean put(String key, CacheValue<T> value) {
        if (key == null) {
            return false;
        }
        cache.put(key, value);
        return true;
    }

    public int getCacheMaxSize() {
        return cacheMaxSize;
    }

    public void setCacheMaxSize(int cacheMaxSize) {
        this.cacheMaxSize = cacheMaxSize;
    }

    private static Lock getLock(String key) {
        Lock lock = lockMap.getIfPresent(key);
        if (lock == null) {
            lock = new ReentrantLock();
            lockMap.put(key, lock);
        }
        return lock;
    }
}
