package com.huifu.virgo.listener;

import com.huifu.virgo.common.base.BaseUtils;
import com.huifu.virgo.common.utils.UrlValid;
import com.huifu.virgo.remote.model.MerchantNotifyMessage;
import com.huifu.virgo.remote.model.SendMsg;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by jianfei.chen on 2015/2/15.
 */
public class SendMsgTest {

    @Test
    public void testSendMsg() {
        String sendMsgJson = "{\"errorType\":0,\"merId\":\"abcd\",\"ordId\":\"7777\",\"postData\":\"callbackType=hello&code=000&message=success&extSysId=abcd&extSeqId=7777&time=1423975824&sign=fM4BGhH3ysEeYRRA4CxTWrO+cgcxpfkaBzfyCgoeY9PV6N2t4sK7sksbyg52iNha5Rx7aWS5iLxfKqUa9lTq9eOC5p81WR2+8hl5gVxQig7XF/vsChj2Cw0KISMHl9x74/JsUJTejcehb1QGwpS8HGY0+eKecVxlNC7IasAw+pE=&data=eyJuYW1lIjoiamFjayIsImFnZSI6MjJ9\",\"sendDate\":\"20150215\",\"sysId\":\"88\",\"sysTxnId\":\"888888\",\"transStat\":\"S\",\"url\":\"http://www.黄页.com:8080/athena-callback/\"}";
        ObjectMapper jsonMapper = new ObjectMapper();
        try {
            MerchantNotifyMessage mnmsg = jsonMapper.readValue(sendMsgJson, SendMsg.class);
            Assert.assertTrue(BaseUtils.validateData(mnmsg));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void testUrl() {
        String[] schemes = {"http", "https"};
        UrlValid urlValidator = new UrlValid(schemes, UrlValid.ALLOW_LOCAL_URLS + UrlValid.ALLOW_2_SLASHES);
        String url = "http://www.黄页.com";
        Assert.assertTrue(urlValidator.isValid(url));

    }
}
