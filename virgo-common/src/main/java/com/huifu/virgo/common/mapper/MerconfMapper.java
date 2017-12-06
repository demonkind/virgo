package com.huifu.virgo.common.mapper;

import com.huifu.virgo.common.exception.VirgoPersistException;
import com.huifu.virgo.remote.model.MerConfSearchForm;
import com.huifu.virgo.remote.model.MerConfiguration;
import com.huifu.virgo.remote.model.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerconfMapper {

    List<MerConfiguration> selectAllMerconf(@Param("page") Page page,@Param("form") MerConfSearchForm form) throws VirgoPersistException;

    MerConfiguration selectOneMerConf(@Param("id") Integer id) throws VirgoPersistException;

    int createOneConf(@Param("form") MerConfiguration form) throws VirgoPersistException;

    int updateConf(@Param("form") MerConfiguration form) throws VirgoPersistException;

    void deleteOneConf(@Param("id") Integer id) throws VirgoPersistException;

    int existMerConf(@Param(value = "sysId") String sysId, @Param(value = "merId") String merId);

    int deleteMerConf(@Param(value = "sysId") String sysId, @Param(value = "merId") String merId);

    MerConfiguration selectMerConf(@Param(value = "sysId") String sysId, @Param(value = "merId") String merId);

    List<MerConfiguration> getMerConfList();

    Integer selectAllMerconfCount(@Param("form")MerConfSearchForm merConfSearchForm);
}
