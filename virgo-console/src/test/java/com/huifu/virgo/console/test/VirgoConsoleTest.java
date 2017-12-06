package com.huifu.virgo.console.test;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.huifu.virgo.console.server.ConsolePegasusServerImpl;
import com.huifu.virgo.remote.model.MerConfiguration;

public class VirgoConsoleTest {

    private static Logger      LOG = LoggerFactory.getLogger(VirgoConsoleTest.class);
    private ApplicationContext context;

    @Test
    public void testSetZkProperties() throws Exception {
        context = new ClassPathXmlApplicationContext(new String[] { "classpath*:applicationContext-console.xml" });
        ConsolePegasusServerImpl server = (ConsolePegasusServerImpl) context.getBean("consolePegasusServerImpl");
        MerConfiguration mc = new MerConfiguration();
        mc.setMaxConsumer(1);
        String qName = "test";
        // server.setZkProperties(mc, qName);
    }
}
