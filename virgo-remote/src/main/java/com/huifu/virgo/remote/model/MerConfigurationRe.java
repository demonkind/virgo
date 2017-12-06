package com.huifu.virgo.remote.model;

import java.io.Serializable;
import java.util.List;

public class MerConfigurationRe implements Serializable{

    /**  */
    private static final long serialVersionUID = -5620094059868853156L;

    private  List<MerConfiguration> data;

    private Page          page;

    public List<MerConfiguration> getData() {
        return data;
    }

    public void setData(List<MerConfiguration> data) {
        this.data = data;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

}
