package com.huifu.virgo.common.utils;

import com.huifu.saturn.cache.client.CacheTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by jianfei.chen on 2015/3/11.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:applicationContext-cache-test.xml"})
public class DelCache {

    @Autowired
    private CacheTemplate cacheTemplate;

    @Test
    public void del() {
        cacheTemplate.delete("VIRGO_CONF_1");
    }
}
