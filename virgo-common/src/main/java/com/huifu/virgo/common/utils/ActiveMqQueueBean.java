package com.huifu.virgo.common.utils;

/**
 * Created by jianfei.chen on 2015/5/15.
 */
public class ActiveMqQueueBean {

    private Integer queueSize;
    private Integer consumerCount;

    public Integer getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(Integer queueSize) {
        this.queueSize = queueSize;
    }

    public Integer getConsumerCount() {
        return consumerCount;
    }

    public void setConsumerCount(Integer consumerCount) {
        this.consumerCount = consumerCount;
    }
}
