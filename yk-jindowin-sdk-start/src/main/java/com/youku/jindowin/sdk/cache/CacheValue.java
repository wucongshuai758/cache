package com.youku.jindowin.sdk.cache;

import java.io.Serializable;

/**
 * @author 吴聪帅
 * @Description
 * @Date : 下午6:14 2019/4/23 Modifyby:
 **/
public class CacheValue<T> implements Serializable {
    private static final long serialVersionUID = -8408553887349833117L;

    private Long time;

    private T data;

    private Long expireTime;

    public CacheValue(T data, Long expireTime) {
        this.time = System.currentTimeMillis();
        this.data = data;
        this.expireTime = expireTime;
    }

    public Long getExpireTime() {
        return expireTime;
    }


    public T getData() {
        return data;
    }

    public static <T> boolean isExpire(CacheValue<T> cacheValue) {
        if (cacheValue.expireTime != null) {
            return (Math.abs(System.currentTimeMillis() - cacheValue.time)) > cacheValue.expireTime;
        }
        return false;
    }

}
