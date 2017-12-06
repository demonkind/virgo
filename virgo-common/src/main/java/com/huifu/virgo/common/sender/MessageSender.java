/**
 *汇付天下有限公司
 * Copyright (c) 2006-2012 ChinaPnR,Inc.All Rights Reserved.
 */
package com.huifu.virgo.common.sender;

import com.huifu.virgo.remote.model.SendMsg;

/**
 * 
 * @author 
 * @version $Id: MessageSender.java, v 0.1 2012-8-24 下午05:28:40 su.zhang Exp $
 */
public interface MessageSender {
    public void dispatch(final SendMsg msg);
}
