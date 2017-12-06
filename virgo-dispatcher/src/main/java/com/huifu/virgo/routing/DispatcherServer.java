package com.huifu.virgo.routing;

import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author
 */
public class DispatcherServer {
    static Logger logger = LoggerFactory.getLogger(DispatcherServer.class);

    public static void main(String[] args) throws Exception {
        try {
            new ClassPathXmlApplicationContext(new String[] { "classpath*:applicationContext.xml" });
            logger.info("Virgo dispatcher server start successfully");
        } catch (Exception e) {
            logger.error("Initiate dispatcher failed.", e);
        }
    }
}
