package com.huifu.virgo.common.mapper;

import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAL层测试基类.
 */
@ContextConfiguration({"classpath:applicationContext-db-test.xml"})
@TestExecutionListeners(value = {
        ServletTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        SqlScriptsTestExecutionListener.class,
        TransactionDbUnitTestExecutionListener.class}, inheritListeners = false)
@Transactional
public class AbstractDbUnitDalTests extends AbstractTransactionalJUnit4SpringContextTests {
}
