package com.huifu.virgo.common.mapper;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.huifu.virgo.remote.model.VirgoConfiguration;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by jianfei.chen on 2015/3/10.
 */
@DatabaseSetup("VirgoConfigData.xml")
public class VirgoconfMapperTest extends AbstractDbUnitDalTests {

    @Autowired
    private VirgoconfMapper virgoconfMapper;

    @Test
    public void selectOneVirgoConf() {
        int id = 1;
        VirgoConfiguration virgoConfiguration = virgoconfMapper.selectOneVirgoConf(id);
        assertThat(virgoConfiguration).isNotNull();
        assertThat(virgoConfiguration.getBlackList()).isEqualTo("http://127.0.0.1,http://127.0.0.2");
    }

    @Test
    public void updateVirgoConf() {
        VirgoConfiguration virgoConfiguration = new VirgoConfiguration();
        virgoConfiguration.setId(1);
        virgoConfiguration.setBlackList("http://127.0.0.3,http://127.0.0.4");
        int eRow = virgoconfMapper.updateVirgoConf(virgoConfiguration);
        assertThat(eRow).isEqualTo(1);
    }
}
