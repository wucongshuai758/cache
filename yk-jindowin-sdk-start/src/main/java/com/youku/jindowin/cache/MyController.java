package com.youku.jindowin.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 吴聪帅
 * @Description
 * @Date : 下午4:29 2019/4/23 Modifyby:
 **/
@RestController
public class MyController {
    @Autowired
    MyService myService;
    @RequestMapping("/hello")
    public String hello() {
        return myService.test();
    }
}
