package com.huifu.virgo.remote.model;

import java.io.Serializable;

public class MerConfSearchForm implements Serializable {

    /**  */
    private static final long serialVersionUID = -556345036419962763L;

    private MerConfiguration  merConfiguration;

    private String            qName;

    public MerConfiguration getMerConfiguration() {
        return merConfiguration;
    }

    public void setMerConfiguration(MerConfiguration merConfiguration) {
        this.merConfiguration = merConfiguration;
    }

    public String getqName() {
        return qName;
    }

    public void setqName(String qName) {
        this.qName = qName;
    }

    String sysId;

    String merId;

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

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

}
