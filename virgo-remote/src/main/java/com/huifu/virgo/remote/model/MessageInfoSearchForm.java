package com.huifu.virgo.remote.model;

import java.io.Serializable;

public class MessageInfoSearchForm implements Serializable {

    /**  */
    private static final long serialVersionUID = -8740412919193463566L;
    private Integer           msgId;
    private String            sendDateStart;

    private String            sendDateEnd;

    private String           sendDateS;

    private String           sendDateE;

    public Integer getMsgId() {
        return msgId;
    }

    public void setMsgId(Integer msgId) {
        this.msgId = msgId;
    }

    public String getSendDateStart() {
        return sendDateStart;
    }

    public void setSendDateStart(String sendDateStart) {
        this.sendDateStart = sendDateStart;
    }

    public String getSendDateEnd() {
        return sendDateEnd;
    }

    public void setSendDateEnd(String sendDateEnd) {
        this.sendDateEnd = sendDateEnd;
    }

    String sysId;

    String merId;

    String sysTxnId;

    String ordId;

    public String getSysTxnId() {
        return sysTxnId;
    }

    public void setSysTxnId(String sysTxnId) {
        this.sysTxnId = sysTxnId;
    }

    public String getOrdId() {
        return ordId;
    }

    public void setOrdId(String ordId) {
        this.ordId = ordId;
    }

    private String sendStat;

    public String getSysId() {
        return sysId;
    }

    public void setSysId(String sysId) {
        this.sysId = sysId;
    }

    public String getMerId() {
        return merId;
    }

    public void setMerId(String merId) {
        this.merId = merId;
    }

    public String getSendStat() {
        return sendStat;
    }

    public void setSendStat(String sendStat) {
        this.sendStat = sendStat;
    }

    public String getSendDateS() {
        return sendDateS;
    }

    public void setSendDateS(String sendDateS) {
        this.sendDateS = sendDateS;
    }

    public String getSendDateE() {
        return sendDateE;
    }

    public void setSendDateE(String sendDateE) {
        this.sendDateE = sendDateE;
    }
}
