package com.huifu.virgo.listener;

import com.huifu.virgo.common.base.BaseUtils;
import com.huifu.virgo.common.base.EnumUtils;
import com.huifu.virgo.common.base.StringValues;
import com.huifu.virgo.common.biz.VirgoconfBiz;
import com.huifu.virgo.common.exception.VirgoException;
import com.huifu.virgo.common.mapper.DispatcherSendMsgMapper;
import com.huifu.virgo.common.mapper.MerconfMapper;
import com.huifu.virgo.common.sender.MessageSender;
import com.huifu.virgo.common.utils.CheckUtils;
import com.huifu.virgo.remote.model.MerConfiguration;
import com.huifu.virgo.remote.model.MerchantNotifyMessage;
import com.huifu.virgo.remote.model.SendMsg;
import com.huifu.virgo.remote.model.VirgoConfiguration;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.io.IOException;
import java.util.Date;

public class WaitDispatcherListener implements MessageListener {

    @Autowired
    private MessageSender messageSender;

    @Autowired
    private DispatcherSendMsgMapper dispatcherSendMsgMapper;

    @Autowired
    private MerconfMapper merconfMapper;

    @Autowired
    private VirgoconfBiz virgoconfBiz;

    private Logger logger = LoggerFactory.getLogger(WaitDispatcherListener.class);
    private ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    @Transactional
    public void onMessage(Message message) {

        MerchantNotifyMessage mnmsg = null;
        String dataJsonString = null;
        SendMsg smsg = new SendMsg();

        // throw VirgoException mean that message parse failed, then record and discard it
        // throw VirgoRuntimeException mean will try process it again
        try {
            // message验证JSON对象
            try {
                dataJsonString = ((TextMessage) message).getText();
            } catch (JMSException e1) {
                throw new VirgoException("非文本消息");
            }
            // 组装JSON对象
            // log and discard in case of JsonParseException, JsonMappingException
            try {
                mnmsg = jsonMapper.readValue(dataJsonString, SendMsg.class);
            } catch (IOException e) {
                throw new VirgoException(e.getLocalizedMessage());
            }
            // 验证数据
            if (!BaseUtils.validateData(mnmsg)) {
                throw new VirgoException("验证字符串失败");
            }
            // 复制为内部对象
            try {
                BeanUtils.copyProperties(mnmsg, smsg);
            } catch (BeansException e) {
                throw new VirgoException("复制对象错误");
            }

            String queuesName = BaseUtils.getQueuesName(smsg.getSysId(), smsg.getMerId());

            // set some default field
            smsg.setDataJson(dataJsonString);
            smsg.setReSendCnt(0);

            MerConfiguration merConfDO = merconfMapper.selectMerConf(smsg.getSysId(), smsg.getMerId());

            //全局黑名单
            if (checkMerUrlFromVirgoBlackList(smsg)) return;

            //商户白名单
            if (checkMerUrlFromMerWhiteList(smsg, merConfDO)) return;

            //商户黑名单
            if (checkMerUrlFromMerBlackList(smsg, merConfDO)) return;


            // 如果关闭，则不分发，直接入库，状态为U
            if (merConfDO != null && merConfDO.isStopFlag()) {
                smsg.setSendStat(StringValues.SEND_STAT_U);
                dispatcherSendMsgMapper.insertSendMsg(smsg);
                return;
            }

            // 入库
            smsg.setSendStat(StringValues.SEND_STAT_Q);
            dispatcherSendMsgMapper.insertSendMsg(smsg);
            // 分发到不同的队列
            messageSender.dispatch(smsg);
        } catch (VirgoException e) {
            insertError(smsg, EnumUtils.DispatherErrorType.VirgoException.getCode(), dataJsonString);
            logger.error("Message validate error!", e);
        }
    }

    public boolean checkMerUrlFromMerBlackList(SendMsg smsg, MerConfiguration merConfDO) {
        if (merConfDO != null) {
            String urlblacklist = merConfDO.getUrlblacklist();
            String urlWhitelist = merConfDO.getUrlwhitelist();
            if (StringUtils.isNotBlank(urlblacklist) && StringUtils.isBlank(urlWhitelist)) {
                if (CheckUtils.isList(urlblacklist, smsg.getUrl())) {
                    logger.warn("商户{}的请求连接{}在商户黑名单中{}", smsg.getMerId(), smsg.getUrl(), urlblacklist);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkMerUrlFromVirgoBlackList(SendMsg smsg) {
        VirgoConfiguration virgoConfiguration = virgoconfBiz.selectOneVirgoConfFromCache(1);
        if (virgoConfiguration != null && StringUtils.isNotBlank(virgoConfiguration.getBlackList())) {
            if (CheckUtils.isList(virgoConfiguration.getBlackList(), smsg.getUrl())) {
                logger.warn("商户{}的请求连接{}在全局黑名单中{}", smsg.getMerId(), smsg.getUrl(), virgoConfiguration.getBlackList());
                return true;
            }
        }
        return false;
    }

    public boolean checkMerUrlFromMerWhiteList(SendMsg smsg, MerConfiguration merConfDO) {

        if (merConfDO != null) {
            String urlWhitelist = merConfDO.getUrlwhitelist();
            if (StringUtils.isNotBlank(urlWhitelist)) {
                if (!CheckUtils.isList(urlWhitelist, smsg.getUrl())) {
                    logger.warn("商户{}的请求连接{}不在商户白名单中{}", smsg.getMerId(), smsg.getUrl(), urlWhitelist);
                    return true;
                }
            }
        }
        return false;
    }

    private void insertError(SendMsg smsg, int errorCode, String errorJson) {
        smsg.setErrorType(EnumUtils.DispatherErrorType.VirgoException.getCode());
        smsg.setErrorJson(errorJson);
        smsg.setErrorCreateDate(new Date());
        dispatcherSendMsgMapper.insertErrorSendMsg(smsg);
    }

}
