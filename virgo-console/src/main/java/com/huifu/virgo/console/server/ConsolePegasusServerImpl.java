package com.huifu.virgo.console.server;

import com.huifu.virgo.common.base.BaseUtils;
import com.huifu.virgo.common.base.StringValues;
import com.huifu.virgo.common.biz.VirgoconfBiz;
import com.huifu.virgo.common.mapper.DispatcherSendMsgMapper;
import com.huifu.virgo.common.mapper.DlqMapper;
import com.huifu.virgo.common.mapper.MerconfMapper;
import com.huifu.virgo.common.mapper.VirgoconfMapper;
import com.huifu.virgo.common.sender.MessageSender;
import com.huifu.virgo.common.utils.ActiveMqJmxManager;
import com.huifu.virgo.remote.IVirgoPegasusServer;
import com.huifu.virgo.remote.model.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.DateValidator;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ConsolePegasusServerImpl implements IVirgoPegasusServer {

    @Autowired
    DispatcherSendMsgMapper dispatcherSendMsgMapper;

    @Autowired
    MessageSender messageSender;

    @Autowired
    MerconfMapper merconfMapper;

    @Autowired
    VirgoconfMapper virgoconfMapper;

    @Autowired
    VirgoconfBiz virgoconfBiz;

    @Autowired
    DlqMapper dlpMapper;

    private ActiveMqJmxManager mqJmxMgr = null;

    private ObjectMapper jsonMapper = new ObjectMapper();

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public SendMsgRe searchMessageInfo(Page page, MessageInfoSearchForm messageInfoSearchFrom) {
        SendMsgRe reDate = new SendMsgRe();

        setSearchFrom(messageInfoSearchFrom);
        Integer count = dispatcherSendMsgMapper.selectAllMessageCount(messageInfoSearchFrom);

        page.setTotalCount(count);
        page.setTotalPage((int) Math.ceil(count * 1.0 / page.getPageSize()));
        page.setLimit((page.getCurrentPage() - 1) * page.getPageSize());

        List<SendMsg> sendMsgs = dispatcherSendMsgMapper.selectAllMessage(page, messageInfoSearchFrom);

        reDate.setData(sendMsgs);
        reDate.setPage(page);
        return reDate;
    }

    public void reSend(Integer msgId) {
        SendMsg sendMsg = dispatcherSendMsgMapper.selectById(msgId);
        sendMsg.setReSendCnt(0);
        sendMsg.setSendStat(StringValues.SEND_STAT_Q);
        messageSender.dispatch(sendMsg);
        cleanSendMsg(sendMsg);
        dlpMapper.updateSendMsg(sendMsg);
    }

    private void setSearchFrom(MessageInfoSearchForm messageInfoSearchFrom) {

        try {
            messageInfoSearchFrom.setSendDateS(DateValidator.getInstance().format(
                    DateValidator.getInstance().validate(messageInfoSearchFrom.getSendDateStart(), BaseUtils.SIMPLE_DATE_FORMAT_YMD), BaseUtils.SEND_DATE_FORMAT));
            messageInfoSearchFrom.setSendDateE(DateValidator.getInstance().format(
                    DateValidator.getInstance().validate(messageInfoSearchFrom.getSendDateEnd(), BaseUtils.SIMPLE_DATE_FORMAT_YMD), BaseUtils.SEND_DATE_FORMAT));
        } catch (Exception e) {

        }
        if (StringUtils.isBlank(messageInfoSearchFrom.getMerId()))
            messageInfoSearchFrom.setMerId(null);
        if (StringUtils.isBlank(messageInfoSearchFrom.getSysId()))
            messageInfoSearchFrom.setSysId(null);
        if (StringUtils.isBlank(messageInfoSearchFrom.getSysTxnId()))
            messageInfoSearchFrom.setSysTxnId(null);
        if (StringUtils.isBlank(messageInfoSearchFrom.getOrdId()))
            messageInfoSearchFrom.setOrdId(null);
        if (StringUtils.isBlank(messageInfoSearchFrom.getSendStat()))
            messageInfoSearchFrom.setSendStat(null);
    }

    private void cleanSendMsg(SendMsg sendMsg) {
        sendMsg.setPostData(null);
        sendMsg.setUrl(null);
    }

    public void reSendAll(MessageInfoSearchForm messageInfoSearchFrom) {
        setSearchFrom(messageInfoSearchFrom);
        List<SendMsg> sendMsgs = dispatcherSendMsgMapper.selectAllMessageNoPage(messageInfoSearchFrom);
        for (SendMsg sendMsg : sendMsgs) {
            sendMsg.setReSendCnt(0);
            sendMsg.setSendStat(StringValues.SEND_STAT_Q);
            messageSender.dispatch(sendMsg);
            cleanSendMsg(sendMsg);
            dlpMapper.updateSendMsg(sendMsg);
        }
    }

    @Override
    public VirgoConfiguration selectOneVirgoConf(Integer id) throws Exception {
        return virgoconfMapper.selectOneVirgoConf(id);
    }

    @Override
    public int updateVirgoConf(VirgoConfiguration form) throws Exception {
        int eRow = virgoconfMapper.updateVirgoConf(form);
        if (eRow == 1) virgoconfBiz.delCache(form.getId());
        return eRow;
    }

    public void setZkProperties(MerConfiguration form) throws Exception {
        if (form.getId() != null)
            merconfMapper.updateConf(form);
        else
            merconfMapper.createOneConf(form);
    }


    public MerConfigurationRe  getAllMerConf(Page page,MerConfSearchForm merConfSearchForm) {
        MerConfigurationRe result = new MerConfigurationRe();
        Integer count = merconfMapper.selectAllMerconfCount(merConfSearchForm);
        page.setTotalCount(count);
        page.setTotalPage((int) Math.ceil(count * 1.0 / page.getPageSize()));
        page.setLimit((page.getCurrentPage() - 1) * page.getPageSize());

        List<MerConfiguration> list = merconfMapper.selectAllMerconf(page,merConfSearchForm);
        result.setData(list);
        result.setPage(page);
        return result;
    }

    public MerConfiguration getOneMerConf(Integer id) throws Exception {
        return merconfMapper.selectOneMerConf(id);
    }

    public void delete(Integer id) throws Exception {
        merconfMapper.deleteOneConf(id);
    }

    public void disable(Integer id) throws Exception {
        MerConfiguration selectOneMerConf = merconfMapper.selectOneMerConf(id);
        selectOneMerConf.setStopFlag(true);
        merconfMapper.updateConf(selectOneMerConf);
        dispatcherSendMsgMapper.updateStatBySysIdAndMerId(selectOneMerConf.getSysId(), selectOneMerConf.getMerId(), StringValues.SEND_STAT_Q, StringValues.SEND_STAT_U);
    }

    public void enable(Integer id) throws Exception {
        MerConfiguration selectOneMerConf = merconfMapper.selectOneMerConf(id);
        selectOneMerConf.setStopFlag(false);
        merconfMapper.updateConf(selectOneMerConf);
    }

    @Override
    public QueryMsg getMessageInfo(Integer msgId) throws Exception {
        QueryMsg queryMsg = new QueryMsg();
        SendMsg sendMsg = dispatcherSendMsgMapper.selectById(msgId);
        queryMsg.setSendMsg(sendMsg);
        return queryMsg;
    }
}
