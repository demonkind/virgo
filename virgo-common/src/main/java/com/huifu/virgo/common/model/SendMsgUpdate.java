package com.huifu.virgo.common.model;

public class SendMsgUpdate {
    private String  lastSendResult;

    private String  lastSendTime;

    private Integer id;

    private String sendStat;
    
    private Integer reSendCnt;

    public String getLastSendResult() {
        return lastSendResult;
    }

    public void setLastSendResult(String lastSendResult) {
        this.lastSendResult = lastSendResult;
    }

    public String getLastSendTime() {
        return lastSendTime;
    }

    public void setLastSendTime(String lastSendTime) {
        this.lastSendTime = lastSendTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSendStat() {
        return sendStat;
    }

    public void setSendStat(String sendStat) {
        this.sendStat = sendStat;
    }

    public Integer getReSendCnt() {
        return reSendCnt;
    }

    public void setReSendCnt(Integer reSendCnt) {
        this.reSendCnt = reSendCnt;
    }

}
