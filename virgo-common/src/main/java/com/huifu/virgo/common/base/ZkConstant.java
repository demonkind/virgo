package com.huifu.virgo.common.base;

import org.apache.curator.framework.CuratorFramework;


public class ZkConstant {

    public static final String ROOT_PATH        = "/virgo";
    public static final String QUEUE_PATH       = "/virgo/queue";
    public static final String SENDER_PATH      = "/virgo/sender";
    public static final String ASSIGN_PATH      = "/virgo/assign";
    public static final String LEADER_PATH      = "/virgo/leader";
    public static final String QUEUE_PREFIX      = "gsend.";
    

    public static String       ConnectionString = "172.16.103.180:2181";
    public static String       MqBrokerUrl      = "failover:(" + "tcp://172.16.103.180:61616" + ")?initialReconnectDelay=1000&startupMaxReconnectAttempts=10&timeout=2000&jms.prefetchPolicy.queuePrefetch=1";
    public static String       MqJmxUrl         = "172.16.101.94,1616,admin,activemq";

    public static int          ScaleupThreshold = 10;
    public static int          ScaleupInterval = 10;

    public static void makeZkPath(CuratorFramework client) throws Exception {
        if (client.checkExists().forPath(ZkConstant.ROOT_PATH) == null) {
            client.create().forPath(ZkConstant.ROOT_PATH);
        }
        if (client.checkExists().forPath(ZkConstant.QUEUE_PATH) == null) {
            client.create().creatingParentsIfNeeded().forPath(ZkConstant.QUEUE_PATH);
        }
        if (client.checkExists().forPath(ZkConstant.SENDER_PATH) == null) {
            client.create().creatingParentsIfNeeded().forPath(ZkConstant.SENDER_PATH);
        }
        if (client.checkExists().forPath(ZkConstant.ASSIGN_PATH) == null) {
            client.create().creatingParentsIfNeeded().forPath(ZkConstant.ASSIGN_PATH);
            // the default value is ip when node create
            client.setData().forPath(ZkConstant.ASSIGN_PATH, "".getBytes());

        }
        if (client.checkExists().forPath(ZkConstant.LEADER_PATH) == null) {
            client.create().creatingParentsIfNeeded().forPath(ZkConstant.LEADER_PATH);
        }
    }
}
