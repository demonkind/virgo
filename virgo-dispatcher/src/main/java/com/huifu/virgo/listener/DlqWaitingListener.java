package com.huifu.virgo.listener;

import com.huifu.virgo.common.base.BaseUtils;
import com.huifu.virgo.common.base.StringValues;
import com.huifu.virgo.common.exception.VirgoRuntimeException;
import com.huifu.virgo.common.mapper.DlqMapper;
import com.huifu.virgo.common.mapper.MerconfMapper;
import com.huifu.virgo.common.sender.MessageSender;
import com.huifu.virgo.remote.model.MerConfiguration;
import com.huifu.virgo.remote.model.SendMsg;
import org.apache.commons.validator.routines.DateValidator;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.Date;

public class DlqWaitingListener implements MessageListener {

    @Autowired
    private MessageSender messageSender;

    @Autowired
    private DlqMapper dlqMapper;

    @Autowired
    private MerconfMapper merconfMapper;

    private Logger logger = LoggerFactory.getLogger(DlqWaitingListener.class);

    private ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public void onMessage(Message message) {

        SendMsg smsg = null;
        String dataJsonString = null;
        try {

            // message验证JSON对象
            dataJsonString = ((TextMessage) message).getText();
            // 组装JSON对象
            smsg = jsonMapper.readValue(dataJsonString, SendMsg.class);

            MerConfiguration merConfDO = merconfMapper.selectMerConf(smsg.getSysId(), smsg.getMerId());

            // 如果关闭，则不重发
            int resendCountMax = 3;
            if (merConfDO != null && merConfDO.isStopFlag()) {
                resendCountMax = merConfDO.getRetryBom();
                smsg.setSendStat(StringValues.SEND_STAT_F);
                dlqMapper.updateSendMsg(smsg);
                return;
            }

            int resendCount = 0;
            if (smsg.getReSendCnt() != null) {
                resendCount = smsg.getReSendCnt();
            }

            if (resendCount >= resendCountMax) {
                // 更新状态为错误
                smsg.setSendStat(StringValues.SEND_STAT_F);
                dlqMapper.updateSendMsg(smsg);
            } else {
                // 更新状态和时间
                int updateReSendCnt = resendCount + 1;
                String lastSendTime = DateValidator.getInstance().format(
                        new Date(), BaseUtils.SIMPLE_DATE_FORMAT);
                smsg.setReSendCnt(updateReSendCnt);
                smsg.setLastSendTime(lastSendTime);
                dlqMapper.updateSendMsg(smsg);
                // 分发到不同的队列
                //smsg.setDataJson(dataJsonString);
                messageSender.dispatch(smsg);
            }
        } catch (Exception e) {
            logger.error("DLQ error!", e);
            throw new VirgoRuntimeException(e);
        }

    }

}
