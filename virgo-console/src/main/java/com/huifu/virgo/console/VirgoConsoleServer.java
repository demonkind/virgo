package com.huifu.virgo.console;

/**
 * 
 *汇付天下有限公司
 * Copyright (c) 2006-2013 ChinaPnR,Inc.All Rights Reserved.
 */

import java.io.IOException;
import java.util.Map;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author Frank.Yan
 */
public class VirgoConsoleServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        new ClassPathXmlApplicationContext(new String[] { "classpath*:applicationContext-console.xml" });
        System.out.println("Example server started...");
        while (System.in.read() != 'X') {
            Thread.sleep(1000);
        }
    }

}
