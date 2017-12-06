/**
 *汇付天下有限公司
 * Copyright (c) 2006-2012 ChinaPnR,Inc.All Rights Reserved.
 */
package com.huifu.virgo.common.sender;

import com.huifu.virgo.common.base.BaseUtils;
import com.huifu.virgo.common.base.StringValues;
import com.huifu.virgo.common.mapper.DlqMapper;
import com.huifu.virgo.remote.model.SendMsg;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.DateValidator;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.support.JmsGatewaySupport;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.io.IOException;
import java.util.Date;

public class MessageSenderImpl extends JmsGatewaySupport implements
        MessageSender {

    private Logger logger = LoggerFactory.getLogger(MessageSenderImpl.class);

    @Autowired
    private DlqMapper dlqMapper;

    private ObjectMapper jsonMapper = new ObjectMapper();

    public void dispatch(final SendMsg smsg) {
        this.getJmsTemplate().setPubSubDomain(false);

        // transfer SendMsg object to json string, should never failed
        // no need keep data json str now
        smsg.setDataJson(null);
        smsg.setErrorJson(null);
        String jsonStr = "";
        try {
            jsonStr = jsonMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(smsg);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }

        final String sendStr = jsonStr;
        String destination = BaseUtils.getQueuesName(smsg.getSysId(), smsg.getMerId());
        logger.info("start to send sysid={};merid={}", new Object[]{smsg.getSysId(), smsg.getMerId()});


        try {
            sendR(destination, smsg, sendStr, 0);
        } catch (Exception e) {
            logger.error("sender{},{}Error:{}", new Object[]{smsg.getSysId(), smsg.getMerId(), e.getLocalizedMessage()});
            SendMsg msg = new SendMsg();
            msg.setId(smsg.getId());
            msg.setSendStat(StringValues.SEND_STAT_F);
            msg.setLastSendTime(DateValidator.getInstance().format(new Date(), BaseUtils.SIMPLE_DATE_FORMAT));
            msg.setLastSendResult(StringUtils.abbreviate(e.getLocalizedMessage(), 40));
            dlqMapper.updateSendMsg(msg);
        }


    }

    private void sendR(String destination, final SendMsg smsg, final String sendStr, int i) {
        try {

            send(destination, smsg, sendStr);
        } catch (Exception e) {
            if (i == 3) throw new RuntimeException(e);
            i++;
            logger.info("repeat {} sender sysid={};merid={}", new Object[]{i, smsg.getSysId(), smsg.getMerId()});
            sendR(destination, smsg, sendStr, i);
        }
    }

    private void send(String destination, final SendMsg smsg, final String sendStr) {
        this.getJmsTemplate().send(destination, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                Message message = session.createTextMessage(sendStr);
                message.setStringProperty(StringValues.GSEND_ID,
                        String.valueOf(smsg.getId()));
                message.setIntProperty(StringValues.RE_SEND_CNT, smsg.getReSendCnt());
                return message;
            }
        });
    }
}
