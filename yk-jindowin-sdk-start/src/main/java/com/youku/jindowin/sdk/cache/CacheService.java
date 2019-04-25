package com.youku.jindowin.sdk.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author 吴聪帅
 * @Description 缓存接口抽象
 * @Date : 下午7:56 2019/4/23 Modifyby:
 **/
public interface CacheService {

    /**
     * 获取单个key值
     * @param key
     * @param <T>
     * @return
     */
    <T> T get(String key);

    /**
     * 获取单个key值
     * @param key
     * @param valueLoader
     * @return
     */
    <T> T get(String key, Callable<T> valueLoader);

    /**
     * 批量获取
     * @param keys
     * @param valueLoader
     * @param <K>
     * @param <V>
     * @return
     */
    <K, V> Map<K, V> mget(List<K> keys, Callable<Map<K, V>> valueLoader);

    /**
     * 删除
     * @param key
     * @return
     */
    boolean delete(String key);

    /**
     * put数据
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    <T> boolean put(String key, T value);

    /**
     * put数据
     * @param key
     * @param value
     * @param expreTimes
     * @param <T>
     * @return
     */
    <T> boolean put(String key, T value, long expreTimes);


    boolean useRedis();
}
