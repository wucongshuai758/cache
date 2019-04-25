package com.youku.jindowin;

import com.youku.jindowin.cache.CacheApplication;
import com.youku.jindowin.sdk.cache.CacheService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author 吴聪帅
 * @Description
 * @Date : 下午10:47 2019/4/24 Modifyby:
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CacheApplication.class)
public class TestApplication {
    @Autowired
    CacheService cacheService;

    @Test
    public void testCacheService() {
        System.out.println("==="+cacheService.useRedis());

        System.out.println("==="+cacheService.get("1",null));
        cacheService.put("1","2");
        while(true);
    }

}
