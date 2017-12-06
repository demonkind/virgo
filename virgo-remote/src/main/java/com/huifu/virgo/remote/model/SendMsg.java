package com.huifu.virgo.remote.model;

import java.io.Serializable;
import java.util.Date;

public class SendMsg extends MerchantNotifyMessage implements Serializable {

    /**  */
    private static final long serialVersionUID = 2007664198917846465L;

    private Long              id;
    private int               errorType;
    private Date              errorCreateDate;
    private String            errorJson;
    private String            dataJson;
    private String            sendStat;
    private Integer           reSendCnt;
    private String            lastSendResult;
    private String            lastSendTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public int getErrorType() {
        return errorType;
    }

    public void setErrorType(int errorType) {
        this.errorType = errorType;
    }

    public Date getErrorCreateDate() {
        return errorCreateDate;
    }

    public void setErrorCreateDate(Date errorCreateDate) {
        this.errorCreateDate = errorCreateDate;
    }

    public String getErrorJson() {
        return errorJson;
    }

    public void setErrorJson(String errorJson) {
        this.errorJson = errorJson;
    }

    public String getDataJson() {
        return dataJson;
    }

    public void setDataJson(String dataJson) {
        this.dataJson = dataJson;
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