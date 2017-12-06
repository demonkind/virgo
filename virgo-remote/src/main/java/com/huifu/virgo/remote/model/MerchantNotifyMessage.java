package com.huifu.virgo.remote.model;

import java.io.Serializable;

public class MerchantNotifyMessage implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String            sendDate;
    private String            sysId;
    private String            sysTxnId;
    private String            transStat;
    private String            merId;
    private String            ordId;
    private String            url;
    private String            postData;

    public String getSendDate() {
        return sendDate;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    public String getSysId() {
        return sysId;
    }

    public void setSysId(String sysId) {
        this.sysId = sysId;
    }

    public String getSysTxnId() {
        return sysTxnId;
    }

    public void setSysTxnId(String sysTxnId) {
        this.sysTxnId = sysTxnId;
    }

    public String getTransStat() {
        return transStat;
    }

    public void setTransStat(String transStat) {
        this.transStat = transStat;
    }

    public String getMerId() {
        return merId;
    }

    public void setMerId(String merId) {
        this.merId = merId;
    }

    public String getOrdId() {
        return ordId;
    }

    public void setOrdId(String ordId) {
        this.ordId = ordId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPostData() {
        return postData;
    }

    public void setPostData(String postData) {
        this.postData = postData;
    }

    @Override
    public String toString() {
        return sysId + " " + merId + " " + sysTxnId + " " + url;
    }

}
