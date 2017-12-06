package com.huifu.virgo.common.mapper;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.huifu.virgo.remote.model.MerConfiguration;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by jianfei.chen on 2015/3/10.
 */
@DatabaseSetup("MerConfigData.xml")
public class MerconfMapperTest extends AbstractDbUnitDalTests {

    @Autowired
    MerconfMapper merconfMapper;

    @Test
    public void createOneConf() {
        MerConfiguration merConfiguration = new MerConfiguration();
        merConfiguration.setId(-10L);
        merConfiguration.setUrlwhitelist("huifu.com");
        int eRow = merconfMapper.createOneConf(merConfiguration);
        assertThat(eRow).isEqualTo(1);
    }

    @Test
    public void updateConf() {
        MerConfiguration merConfiguration = new MerConfiguration();
        merConfiguration.setId(1L);
        merConfiguration.setUrlwhitelist("huifu.com");
        int eRow = merconfMapper.updateConf(merConfiguration);
        assertThat(eRow).isEqualTo(1);
    }

}
