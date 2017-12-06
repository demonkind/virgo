package com.huifu.virgo.remote.model;

import java.io.Serializable;

public class MerConfiguration implements Serializable {

    //使能状态标志，HTTP请求超时/秒，最大重试次数，重试时间间隔/秒，重试倍增因子，最小线程数，最大线程数，最大重发次数
    // Sys_Id
    // Mer_Id
    // Stop_FLAG
    // Http_Timeout
    // Retry_Max
    // Retry_Delay
    // Retry_Bom
    // Min_TCnt
    // Max_TCnt
    // Resend_Max

    /**  */
    private static final long serialVersionUID = -7298791117110879514L;

    private Long              id;

    private String            sysId;
    private String            merId;
    private boolean           stopFlag;
    private int               httpTimeout;
    private int               retryMax;
    private int               retryDelay;
    private int               retryBom;
    private int               minConsumer;
    private int               maxConsumer;
    private int               resendMax;
    // 工作状态，H/M/L
    private String            currentStatus;
    private String            urlblacklist;
    private String            urlwhitelist;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public boolean isStopFlag() {
        return stopFlag;
    }

    public void setStopFlag(boolean stopFlag) {
        this.stopFlag = stopFlag;
    }

    public int getHttpTimeout() {
        return httpTimeout;
    }

    public void setHttpTimeout(int httpTimeout) {
        this.httpTimeout = httpTimeout;
    }

    public int getRetryMax() {
        return retryMax;
    }

    public void setRetryMax(int retryMax) {
        this.retryMax = retryMax;
    }

    public int getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(int retryDelay) {
        this.retryDelay = retryDelay;
    }

    public int getRetryBom() {
        return retryBom;
    }

    public void setRetryBom(int retryBom) {
        this.retryBom = retryBom;
    }

    public int getMinConsumer() {
        return minConsumer;
    }

    public void setMinConsumer(int minConsumer) {
        this.minConsumer = minConsumer;
    }

    public int getMaxConsumer() {
        return maxConsumer;
    }

    public void setMaxConsumer(int maxConsumer) {
        this.maxConsumer = maxConsumer;
    }

    public int getResendMax() {
        return resendMax;
    }

    public void setResendMax(int resendMax) {
        this.resendMax = resendMax;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getUrlblacklist() {
        return urlblacklist;
    }

    public void setUrlblacklist(String urlblacklist) {
        this.urlblacklist = urlblacklist;
    }


    public String getUrlwhitelist() {
        return urlwhitelist;
    }

    public void setUrlwhitelist(String urlwhitelist) {
        this.urlwhitelist = urlwhitelist;
    }
}
