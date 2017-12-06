package com.huifu.virgo.remote.model;

import java.io.Serializable;

/**
 * Created by jianfei.chen on 2015/3/10.
 */
public class VirgoConfiguration implements Serializable {


    private static final long serialVersionUID = -7565880377914297489L;
    private Integer id;
    private String blackList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBlackList() {
        return blackList;
    }

    public void setBlackList(String blackList) {
        this.blackList = blackList;
    }
}
