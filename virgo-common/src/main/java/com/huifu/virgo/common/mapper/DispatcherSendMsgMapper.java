package com.huifu.virgo.common.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.huifu.virgo.common.exception.VirgoPersistException;
import com.huifu.virgo.remote.model.MessageInfoSearchForm;
import com.huifu.virgo.remote.model.Page;
import com.huifu.virgo.remote.model.SendMsg;

@Repository
public interface DispatcherSendMsgMapper {

    void insertErrorSendMsg(SendMsg msg) throws VirgoPersistException;

    void insertSendMsg(SendMsg msg) throws VirgoPersistException;

    List<SendMsg> selectAllMessage(@Param("page") Page page, @Param("form") MessageInfoSearchForm messageInfoSearchFrom) throws VirgoPersistException;

    Integer selectAllMessageCount(@Param("form") MessageInfoSearchForm messageInfoSearchFrom) throws VirgoPersistException;

    SendMsg selectById(Integer msgId) throws VirgoPersistException;

    void updateStatBySysIdAndMerId(@Param("sysId") String sysId, @Param("merId") String merId, @Param("fromStat") String fromStat, @Param("toStat") String toStat) throws VirgoPersistException;

    List<SendMsg> selectAllMessageNoPage(@Param("form")MessageInfoSearchForm messageInfoSearchFrom) throws VirgoPersistException;

}
