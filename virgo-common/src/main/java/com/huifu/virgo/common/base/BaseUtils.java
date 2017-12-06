package com.huifu.virgo.common.base;

import com.huifu.virgo.common.utils.UrlValid;
import com.huifu.virgo.remote.model.MerConfiguration;
import com.huifu.virgo.remote.model.MerchantNotifyMessage;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.DateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseUtils {

    private static Logger logger = LoggerFactory.getLogger(BaseUtils.class);

    public static String SIMPLE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static String SIMPLE_DATE_FORMAT_YMD = "yyyy-MM-dd";

    public static String SEND_DATE_FORMAT = "yyyyMMdd";

    public static String getQueuesName(String sysId, String merId) {
        return ZkConstant.QUEUE_PREFIX + sysId + "." + merId;
    }

    private static String[] schemes = {"http", "https"};
    private static UrlValid urlValidator = new UrlValid(schemes, UrlValid.ALLOW_LOCAL_URLS + UrlValid.ALLOW_2_SLASHES);

    public static boolean validateData(MerchantNotifyMessage mnmsg) {
        if (StringUtils.isBlank(mnmsg.getSendDate()) || StringUtils.isBlank(mnmsg.getSysId()) || StringUtils.isBlank(mnmsg.getSysTxnId()) || StringUtils.isBlank(mnmsg.getTransStat())
                || StringUtils.isBlank(mnmsg.getMerId()) || StringUtils.isBlank(mnmsg.getOrdId()) || StringUtils.isBlank(mnmsg.getUrl()) || StringUtils.isBlank(mnmsg.getPostData())) {
            logger.error("MerchantNotifyMessage some field is blank. mnmsg=" + mnmsg.toString());
            return false;
        } else {

            // sysid merid must be alpha or numberic not chinese
            if (isChinese(mnmsg.getSysId()) || isChinese(mnmsg.getMerId())) {
                logger.error("Validator Error:Sysid merid must be alpha or numberic not chinese! SysId=" + mnmsg.getSysId() + "merid=" + mnmsg.getMerId());
                return false;
            }

            // sysTxnId merid must be alpha or numberic
            if (!StringUtils.isAlphanumeric(mnmsg.getSysTxnId())) {
                logger.error("Validator Error:Sysid merid must be alpha or numberic! SysTxnId=" + mnmsg.getSysTxnId());
                return false;
            }

            // sysid merid must be alpha or numberic
            if (!StringUtils.isAlphanumeric(mnmsg.getSysId()) || !StringUtils.isAlphanumeric(mnmsg.getMerId())) {
                logger.error("Validator Error:Sysid merid must be alpha or numberic! SysId=" + mnmsg.getSysId() + "merid=" + mnmsg.getMerId());
                return false;
            }
            // send date
            if (!DateValidator.getInstance().isValid(mnmsg.getSendDate(), SEND_DATE_FORMAT)) {
                logger.error("Validator Error:Send date Error! senddate=" + mnmsg.getSendDate());
                return false;
            }
            // url
            if (!urlValidator.isValid(mnmsg.getUrl().trim())) {
                logger.error("Validator Error:Url is Error! url=" + mnmsg.getUrl());
                return false;
            }

            if (mnmsg.getSysTxnId().length() > 50) {
                logger.error("Validator Error:SysTxnId is too long! SysTxnId=" + mnmsg.getSysTxnId());
                return false;
            }

            if (mnmsg.getMerId().length() > 50) {
                logger.error("Validator Error:MerId is too long! MerId=" + mnmsg.getMerId());
                return false;
            }

            if (mnmsg.getOrdId().length() > 50) {
                logger.error("Validator Error:OrdId is too long! OrdId=" + mnmsg.getOrdId());
                return false;
            }
            return true;
        }
    }

    public static String setupGName(MerConfiguration form) {
        return StringValues.GSEND_STRING + "." + form.getSysId() + "." + form.getMerId();
    }

    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    public static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }
}
