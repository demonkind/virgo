package com.huifu.virgo.common.biz;

import com.huifu.cache.client.CPCacheClient;
import com.huifu.virgo.common.mapper.VirgoconfMapper;
import com.huifu.virgo.remote.model.VirgoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by jianfei.chen on 2015/3/11.
 */
@Service
public class VirgoconfBiz {

    private Logger logger = LoggerFactory.getLogger(VirgoconfBiz.class);

    @Autowired
    private VirgoconfMapper virgoconfMapper;

    @Autowired
    private CPCacheClient cpCacheClient;

    public static final String VIRGO_CONF_CACHE_KEY = "mer.config.cache.key";

    public VirgoConfiguration selectOneVirgoConfFromCache(final int id) {
//        logger.info("selectOneVirgoConfFromCache" + id);
        return (VirgoConfiguration)cpCacheClient.getObject(VIRGO_CONF_CACHE_KEY + id);
    }

    public void delCache(int id) {
        cpCacheClient.removeObject(VIRGO_CONF_CACHE_KEY + id);
    }
}
