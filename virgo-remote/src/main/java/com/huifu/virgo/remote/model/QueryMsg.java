package com.huifu.virgo.remote.model;

import java.io.Serializable;

/**
 */
public class QueryMsg  implements Serializable {

    private String code;
    private String msg;
    private SendMsg sendMsg;


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public SendMsg getSendMsg() {
        return sendMsg;
    }

    public void setSendMsg(SendMsg sendMsg) {
        this.sendMsg = sendMsg;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QueryMsg{");
        sb.append("code='").append(code).append('\'');
        sb.append(", msg='").append(msg).append('\'');
        sb.append(", sendMsg=").append(sendMsg);
        sb.append('}');
        return sb.toString();
    }
}
