package com.huifu.virgo.remote;

import com.huifu.virgo.remote.model.*;

import java.util.List;

public interface IVirgoPegasusServer {

    SendMsgRe searchMessageInfo(Page page, MessageInfoSearchForm messageInfoSearchFrom) throws Exception;

    void reSend(Integer msgId) throws Exception;

    void setZkProperties(MerConfiguration merConfiguration) throws Exception;

    MerConfigurationRe getAllMerConf(Page page,MerConfSearchForm merConfSearchForm) throws Exception;

    MerConfiguration getOneMerConf(Integer id) throws Exception;

    void delete(Integer id) throws Exception;

    void disable(Integer id) throws Exception;

    void enable(Integer id) throws Exception;

    void reSendAll(MessageInfoSearchForm messageInfoSearchFrom) throws Exception;

    VirgoConfiguration selectOneVirgoConf(Integer id) throws Exception;

    int updateVirgoConf(VirgoConfiguration form) throws Exception;

    QueryMsg getMessageInfo(Integer msgId) throws Exception;

}
