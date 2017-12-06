package com.huifu.virgo.common.mapper;

import com.huifu.virgo.common.exception.VirgoPersistException;
import com.huifu.virgo.remote.model.VirgoConfiguration;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * Created by jianfei.chen on 2015/3/10.
 */
@Repository
public interface VirgoconfMapper {

    VirgoConfiguration selectOneVirgoConf(@Param("id") Integer id) throws VirgoPersistException;

    int updateVirgoConf(@Param("form") VirgoConfiguration form) throws VirgoPersistException;

}
