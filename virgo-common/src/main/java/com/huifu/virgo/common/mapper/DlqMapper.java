package com.huifu.virgo.common.mapper;

import org.springframework.stereotype.Repository;

import com.huifu.virgo.common.exception.VirgoPersistException;
import com.huifu.virgo.remote.model.SendMsg;

@Repository
public interface DlqMapper {

    void updateSendMsg(SendMsg smsg) throws VirgoPersistException;

}
