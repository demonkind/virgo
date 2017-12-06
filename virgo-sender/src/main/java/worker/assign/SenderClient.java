package worker.assign;

import com.huifu.virgo.common.base.BaseUtils;
import com.huifu.virgo.common.base.EnumUtils;
import com.huifu.virgo.common.base.StringValues;
import com.huifu.virgo.common.base.ZkConstant;
import com.huifu.virgo.common.biz.VirgoconfBiz;
import com.huifu.virgo.common.exception.VirgoException;
import com.huifu.virgo.common.mapper.DispatcherSendMsgMapper;
import com.huifu.virgo.common.mapper.DlqMapper;
import com.huifu.virgo.common.mapper.MerconfMapper;
import com.huifu.virgo.common.sender.MessageSenderImpl;
import com.huifu.virgo.common.utils.ActiveMqJmxManager;
import com.huifu.virgo.common.utils.ActiveMqQueueBean;
import com.huifu.virgo.common.utils.CheckUtils;
import com.huifu.virgo.remote.model.MerConfiguration;
import com.huifu.virgo.remote.model.MerchantNotifyMessage;
import com.huifu.virgo.remote.model.SendMsg;
import com.huifu.virgo.remote.model.VirgoConfiguration;
import com.huifu.virgo.worker.MerQueueListener;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQMessageConsumer;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.DestinationInfo;
import org.apache.activemq.jms.pool.PooledMessageConsumer;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.utils.CloseableUtils;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.*;
import javax.jms.Queue;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SenderClient implements InitializingBean, DisposableBean {


    public SenderClient() {

    }

    private static Logger LOG = LoggerFactory.getLogger(SenderClient.class);
    private DispatcherSendMsgMapper dispatcherSendMsgMapper;


    private VirgoconfBiz virgoconfBiz;
    private MerconfMapper merconfMapper;

    private Map<String, String> queueMap = new ConcurrentHashMap<String, String>();
    private Map<String, MerConfiguration> queueMerConf = new ConcurrentHashMap<String, MerConfiguration>();

    private PooledConnectionFactory mqPooledConnectionFactory;
    private PooledConnectionFactory reveiveConnectionFactory;
    private Connection dispatcherConn;
    private Connection receiveConn;
    private Connection conn;
    private ApplicationContext context;
    private MessageSenderImpl messageSender;

    private ConcurrentHashMap<String, List<ActiveMQMessageConsumer>> activeMQQueueMap = new ConcurrentHashMap<String, List<ActiveMQMessageConsumer>>();

    private ActiveMqJmxManager mqJmxMgr = null;

    protected void startup() throws Exception {
        context = new ClassPathXmlApplicationContext(new String[]{"classpath*:applicationContext-sender.xml"});
        merconfMapper = (MerconfMapper) context.getBean("merconfMapper");
        dispatcherSendMsgMapper = (DispatcherSendMsgMapper) context.getBean("dispatcherSendMsgMapper");
        virgoconfBiz = (VirgoconfBiz) context.getBean("virgoconfBiz");
        messageSender = (MessageSenderImpl) context.getBean("messageSender");
        initReceiveActiveMQ();
        initCreateConnection();
        initDispatcherMQ(25);

        addAdvisoryQueueListener(1);

        initMer();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    doJmx();
                } catch (Exception e) {
                    LOG.error("taskDoJmx", e);
                }
            }
        });
        thread.setDaemon(true);

        ScheduledExecutorService executorService = Executors
                .newScheduledThreadPool(2);
        executorService.scheduleAtFixedRate(thread, 10, 10, TimeUnit.SECONDS);

    }

    private void initDispatcherMQ(int num) {
        try {
            for (int i = 0; i < num; i++) {
                PooledConnectionFactory dispatcherConnectionFactory = (PooledConnectionFactory) context.getBean("connectionFactoryDispatcher");
                ActiveMQConnectionFactory activeMQConnectionFactory = (ActiveMQConnectionFactory) dispatcherConnectionFactory.getConnectionFactory();
                QueueConnection connection = activeMQConnectionFactory.createQueueConnection();
                connection.start();

                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Destination destination = session.createQueue("merchant.notify.service");
                ActiveMQMessageConsumer consumer = (ActiveMQMessageConsumer) session.createConsumer(destination);
                consumer.setMessageListener(new WaitDispatcherListener());
            }

        } catch (Exception e) {
            LOG.error("initReceiveActiveMQ failed:", e);
        }
    }

    private void initDispatcherMQCreateListener() {

    }

    private void initReceiveActiveMQ() {
        try {
            reveiveConnectionFactory = (PooledConnectionFactory) context.getBean("receiveActiveMQ");
            receiveConn = reveiveConnectionFactory.createConnection();
            receiveConn.start();
        } catch (Exception e) {
            LOG.error("initReceiveActiveMQ failed:", e);
        }
    }


    private void initCreateConnection() {
        try {
            mqPooledConnectionFactory = (PooledConnectionFactory) context.getBean("acitvemqPCF");
            conn = mqPooledConnectionFactory.createConnection();
            conn.start();
            mqJmxMgr = ActiveMqJmxManager.getInstance();
        } catch (Exception e) {
            LOG.error("initCreateConnection failed:", e);
        }
    }

//    private void initConsumer() {
//        List<MerConfiguration> list = merconfMapper.getMerConfList();
//        if (list != null && !list.isEmpty()) {
//            for (MerConfiguration merConfDO : list) {
//                String qName = "gsend." + merConfDO.getSysId() + "." + merConfDO.getMerId();
//                try {
//                    initConsumerCreate(qName, merConfDO);
//                    addQueueToMap(qName);
//                } catch (Exception e) {
//                    LOG.error("initConsumer failed:", e);
//                }
//
//            }
//        }
//    }
//
//    private void initConsumerCreate(String queueName, MerConfiguration merConfDO) throws Exception {
//        if (!merConfDO.isStopFlag()) {
//            if (!activeMQQueueMap.containsKey(queueName)) {
//                List<ActiveMQMessageConsumer> csumList = new ArrayList<ActiveMQMessageConsumer>();
//                if ("H".equals(merConfDO.getCurrentStatus())) {
//                    for (int i = 0; i < merConfDO.getMaxConsumer(); i++) {
//                        csumList.add(createQueueConsumer(queueName, merConfDO));
//                    }
//                } else {
//                    for (int i = 0; i < merConfDO.getMinConsumer(); i++) {
//                        csumList.add(createQueueConsumer(queueName, merConfDO));
//                    }
//                }
//
//                activeMQQueueMap.put(queueName, csumList);
//                LOG.info("initConsumerCreate Add queue : {}", queueName);
//            }
//        }
//    }

    /////////////////////////// dojmx

    private void taskDoJmx() {

        TimerTask task = new TimerTask() {
            public void run() {
                doJmx();
            }
        };
        Timer timer = new Timer(true);
        timer.schedule(task, 1000 * 10, 1000 * 10);
    }

    public void initMer() {
        List<MerConfiguration> list = merconfMapper.getMerConfList();
        if (list != null) {
            for (MerConfiguration merConfDO : list) {
                String qname = "gsend." + merConfDO.getSysId() + "." + merConfDO.getMerId();
                queueMerConf.put(qname, merConfDO);
            }
        }
    }

    public void doJmx() {
        try {
            LOG.info("start doJmx.");
            List<MerConfiguration> list = merconfMapper.getMerConfList();
            Map<String, ActiveMqQueueBean> queueSizeMap = mqJmxMgr.listQueueBean();


            for (String queueName : queueSizeMap.keySet()) {
//                LOG.info("queueName={}",queueName);
                int consumerCount = 0;
                ActiveMqQueueBean activeMqQueueBean = queueSizeMap.get(queueName);
                consumerCount = activeMqQueueBean.getConsumerCount();
                int qsize = activeMqQueueBean.getQueueSize();
                MerConfiguration merConf = null;

                MerConfiguration merOld = queueMerConf.get(queueName);
                for (MerConfiguration merConfDO : list) {
                    String qname = "gsend." + merConfDO.getSysId() + "." + merConfDO.getMerId();
                    if (qname.equals(queueName)) {
                        if (qsize > ZkConstant.ScaleupThreshold && (merOld != null && !"H".equals(merOld.getCurrentStatus()))) {
                            merConfDO.setCurrentStatus("H");
                            merconfMapper.updateConf(merConfDO);
                            merOld.setMaxConsumer(merConfDO.getMaxConsumer() - 1);
                        }
                        if (qsize == 0 && (merOld != null && "H".equals(merOld.getCurrentStatus()))) {
                            merConfDO.setCurrentStatus("L");
                            merconfMapper.updateConf(merConfDO);
                            merOld.setMinConsumer(merConfDO.getMinConsumer() - 1);
                        }
                        merConf = merConfDO;
                        break;
                    }
                }

//                LOG.info("consumerCount={}",consumerCount);
//                LOG.info("merConf.getMinConsumer()={}",merConf.getMinConsumer());
//                LOG.info("merConf.getCurrentStatus()={}",merConf.getCurrentStatus());
//                LOG.info("merConf.condition={}",(merConf != null && "L".equals(merConf.getCurrentStatus()) && consumerCount != merConf.getMinConsumer()));
                if (consumerCount == 0 ||
                        (merConf != null && merOld != null && "L".equals(merConf.getCurrentStatus()) && merOld.getMinConsumer() != merConf.getMinConsumer()) ||
                        (merConf != null && merOld != null && "H".equals(merConf.getCurrentStatus()) && merOld.getMaxConsumer() != merConf.getMaxConsumer())) {
                    addQueueToDB(queueName);
                    cleanQueueConsumer(queueName);
                    applyQueueAdded(queueName);
                    addQueueToMap(queueName);
                    if (merConf != null) {
                        queueMerConf.put(queueName, merConf);
                    }
                }
            }
            LOG.info("complete doJmx.");
        } catch (Exception e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
    }

//    private synchronized void doJmxApplyQueueAdded(String queueName) throws Exception {
//        String[] tmp = queueName.split("\\.");
//        String sysId = tmp[1];
//        String merId = tmp[2];
//        MerConfiguration merConfDO = merconfMapper.selectMerConf(sysId, merId);
//        if (!merConfDO.isStopFlag()) {
//            if (!activeMQQueueMap.containsKey(queueName)) {
//                List<ActiveMQMessageConsumer> csumList = new ArrayList<ActiveMQMessageConsumer>();
//                if ("H".equals(merConfDO.getCurrentStatus())) {
//                    for (int i = 0; i < merConfDO.getMaxConsumer(); i++) {
//                        csumList.add(doJmxCreateQueueConsumer(queueName, merConfDO));
//                    }
//                } else {
//                    for (int i = 0; i < merConfDO.getMinConsumer(); i++) {
//                        csumList.add(doJmxCreateQueueConsumer(queueName, merConfDO));
//                    }
//                }
//
//                activeMQQueueMap.put(queueName, csumList);
//                LOG.info("Add queue : {}", queueName);
//            }
//        } else {
//            doJmxCleanQueueConsumer(queueName);
//        }
//    }
//
//    private int j = 0;
//
//    private synchronized ActiveMQMessageConsumer doJmxCreateQueueConsumer(String qName, MerConfiguration merConfDO) throws Exception {
//        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
//        redeliveryPolicy.setInitialRedeliveryDelay(0);
//        redeliveryPolicy.setRedeliveryDelay(merConfDO.getRetryDelay() * 1000);
//        redeliveryPolicy.setMaximumRedeliveries(merConfDO.getRetryMax());
//        redeliveryPolicy.setBackOffMultiplier(merConfDO.getRetryBom());
//        redeliveryPolicy.setUseExponentialBackOff(true);
//        redeliveryPolicy.setQueue(qName);
//
//        if (j % 500 == 0) {
//            conn = mqPooledConnectionFactory.createConnection();
//            conn.start();
//        }
//        Session session = conn.createSession(true, Session.CLIENT_ACKNOWLEDGE);
//        Queue queue = session.createQueue(qName);
//        PooledMessageConsumer poolMConsumer = (PooledMessageConsumer) session.createConsumer(queue);
//        ActiveMQMessageConsumer consumer = (ActiveMQMessageConsumer) poolMConsumer.getDelegate();
//        consumer.setRedeliveryPolicy(redeliveryPolicy);
//        MerQueueListener lsnr = new MerQueueListener(session, merConfDO);
//        lsnr.setDlqMapper(context.getBean(DlqMapper.class));
//        consumer.setMessageListener(lsnr);
//        consumer.start();
//        j++;
//        return consumer;
//
//    }
//
//    private synchronized void doJmxCleanQueueConsumer(String qName) throws JMSException {
//        if (activeMQQueueMap.containsKey(qName)) {
//            LOG.info("Stop queue consumer : {}", qName);
//            List<ActiveMQMessageConsumer> csumList = activeMQQueueMap.get(qName);
//            for (ActiveMQMessageConsumer mc : csumList) {
//                mc.stop();
//                MerQueueListener lsnr = (MerQueueListener) (mc.getMessageListener());
//                CloseableUtils.closeQuietly(lsnr);
//            }
//            activeMQQueueMap.get(qName).clear();
//            activeMQQueueMap.remove(qName);
//        }
//    }

    //////////////////////ActiveMQ.Advisory.Queue
    public void addAdvisoryQueueListener(int number) throws Exception {
        Session monitorSession = receiveConn.createSession(true,
                Session.AUTO_ACKNOWLEDGE);
        Destination advisoryDestination = monitorSession
                .createTopic("ActiveMQ.Advisory.Queue");
        MessageConsumer topicConsumer = monitorSession
                .createConsumer(advisoryDestination);
        for (int tp = 0; tp < number; tp++) {
            topicConsumer.setMessageListener(new QueueTopicMonitorListener());
        }

    }

    private class QueueTopicMonitorListener implements MessageListener {

        @Override
        public void onMessage(Message msg) {
            if (msg instanceof ActiveMQMessage) {
                ActiveMQMessage aMsg = (ActiveMQMessage) msg;
                DestinationInfo destinfo = (DestinationInfo) aMsg
                        .getDataStructure();
                String queueName = destinfo.getDestination().getPhysicalName();
                try {
                    if (queueName.startsWith(ZkConstant.QUEUE_PREFIX)) {
                        if (destinfo.getOperationType() == 0
                                && !queueMap.containsKey(queueName)) {
//                            addQueueToDB(queueName);
//                            addQueueToMap(queueName);
//                            applyQueueAdded(queueName);
                        } else if (destinfo.getOperationType() == 1) {

                            removeQueueFromDB(queueName);
                            removeQueueFromMap(queueName);
                        }
                    }
                } catch (Exception e) {
                    LOG.info(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    private synchronized void addQueueToDB(String qName) throws Exception {
        if (qName.contains("/")) {
            LOG.error("Invalid node name" + qName);
            return;
        }
        String[] tmp = qName.split("\\.");
        String sysId = tmp[1];
        String merId = tmp[2];

        MerConfiguration mcdb = merconfMapper.selectMerConf(sysId, merId);
        if (mcdb == null) {
            MerConfiguration mc = new MerConfiguration();
            mc.setSysId(sysId);
            mc.setMerId(merId);
            mc.setStopFlag(false);
            mc.setHttpTimeout(5);
            mc.setMinConsumer(1);
            mc.setMaxConsumer(5);
            mc.setRetryDelay(1);
            mc.setRetryBom(1);
            mc.setRetryMax(3);
            mc.setResendMax(3);
            mc.setCurrentStatus("L");
            try {
                merconfMapper.createOneConf(mc);
                queueMerConf.put(qName, mc);
            } catch (Exception e) {
                //ignore
            }
            LOG.info("insert db mer success,qname={}", qName);
        } else {
            queueMerConf.put(qName, mcdb);
        }
    }

    private synchronized void removeQueueFromDB(String qName) throws Exception {
        String[] tmp = qName.split("\\.");
        String sysId = tmp[1];
        String merId = tmp[2];
        merconfMapper.deleteMerConf(sysId, merId);
        LOG.info("Delete db Queue {}", qName);
    }

    private synchronized void addQueueToMap(String queueName) throws Exception {
        String groupName = queueName.split("\\.")[1];
        queueMap.put(queueName, groupName);
    }

    private synchronized void removeQueueFromMap(String queueName) throws Exception {
        queueMap.remove(queueName);
        queueMerConf.remove(queueName);
        cleanQueueConsumer(queueName);
    }

    private synchronized void applyQueueAdded(String queueName) throws Exception {
        String[] tmp = queueName.split("\\.");
        String sysId = tmp[1];
        String merId = tmp[2];
        MerConfiguration merConfDO = merconfMapper.selectMerConf(sysId, merId);
        if (!merConfDO.isStopFlag()) {
            if (!activeMQQueueMap.containsKey(queueName)) {
                List<ActiveMQMessageConsumer> csumList = new ArrayList<ActiveMQMessageConsumer>();
                if ("H".equals(merConfDO.getCurrentStatus())) {
                    for (int i = 0; i < merConfDO.getMaxConsumer(); i++) {
                        csumList.add(createQueueConsumer(queueName, merConfDO));
                    }
                } else {
                    for (int i = 0; i < merConfDO.getMinConsumer(); i++) {
                        csumList.add(createQueueConsumer(queueName, merConfDO));
                    }
                }

                activeMQQueueMap.put(queueName, csumList);
                LOG.info("Add Consumer queue : {}", queueName);
            }
        } else {
            cleanQueueConsumer(queueName);

        }
    }

    int i = 0;

    private synchronized ActiveMQMessageConsumer createQueueConsumer(String qName, MerConfiguration merConfDO) throws Exception {
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setInitialRedeliveryDelay(0);
        redeliveryPolicy.setRedeliveryDelay(merConfDO.getRetryDelay() * 1000);
        redeliveryPolicy.setMaximumRedeliveries(merConfDO.getRetryMax());
        redeliveryPolicy.setBackOffMultiplier(merConfDO.getRetryBom());
        redeliveryPolicy.setUseExponentialBackOff(true);
        redeliveryPolicy.setQueue(qName);
        if (i % 200 == 0) {
            conn = mqPooledConnectionFactory.createConnection();
            conn.start();
        }
        Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(qName);
        PooledMessageConsumer poolMConsumer = (PooledMessageConsumer) session.createConsumer(queue);
        ActiveMQMessageConsumer consumer = (ActiveMQMessageConsumer) poolMConsumer.getDelegate();
        consumer.setRedeliveryPolicy(redeliveryPolicy);
        MerQueueListener lsnr = new MerQueueListener(session, merConfDO);
        lsnr.setDlqMapper(context.getBean(DlqMapper.class));
        consumer.setMessageListener(lsnr);
        consumer.start();
        i++;
        LOG.info("numbers:{}", i);
        return consumer;

    }


    private synchronized void cleanQueueConsumer(String qName) throws JMSException {
        if (activeMQQueueMap.containsKey(qName)) {
            List<ActiveMQMessageConsumer> csumList = activeMQQueueMap.get(qName);
            for (ActiveMQMessageConsumer mc : csumList) {
                MerQueueListener lsnr = (MerQueueListener) (mc.getMessageListener());
                CloseableUtils.closeQuietly(lsnr);

                try {
                    mc.stop();
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
                try {
                    //lsnr.getSession().rollback();
                    lsnr.getSession().recover();
                    lsnr.getSession().close();
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            activeMQQueueMap.get(qName).clear();
            activeMQQueueMap.remove(qName);
            LOG.info("Delete consumer queue : {}", qName);
        }

    }


    public static void main(String[] args) throws Exception {
        SenderClient sc = new SenderClient();
        sc.startup();
        int read = System.in.read();
        while (true) {
            if (read == 'x') {
                System.exit(0);
            } else {
                Thread.sleep(1000);
            }
        }
    }

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }


    public class WaitDispatcherListener implements MessageListener {

        public WaitDispatcherListener() {

        }


        private Logger logger = LoggerFactory.getLogger(WaitDispatcherListener.class);
        private ObjectMapper jsonMapper = new ObjectMapper();

        @Override
        @Transactional
        public void onMessage(Message message) {

            MerchantNotifyMessage mnmsg = null;
            String dataJsonString = null;
            SendMsg smsg = new SendMsg();

            // throw VirgoException mean that message parse failed, then record and discard it
            // throw VirgoRuntimeException mean will try process it again
            try {
                // message验证JSON对象
                try {
                    dataJsonString = ((TextMessage) message).getText();
                } catch (JMSException e1) {
                    throw new VirgoException("非文本消息");
                }
                // 组装JSON对象
                // log and discard in case of JsonParseException, JsonMappingException
                try {
                    mnmsg = jsonMapper.readValue(dataJsonString, SendMsg.class);
                } catch (IOException e) {
                    throw new VirgoException(e.getLocalizedMessage());
                }
                // 验证数据
                if (!BaseUtils.validateData(mnmsg)) {
                    throw new VirgoException("验证字符串失败");
                }
                // 复制为内部对象
                try {
                    BeanUtils.copyProperties(mnmsg, smsg);
                } catch (BeansException e) {
                    throw new VirgoException("复制对象错误");
                }

                String queuesName = BaseUtils.getQueuesName(smsg.getSysId(), smsg.getMerId());

                // set some default field
                smsg.setDataJson(dataJsonString);
                smsg.setReSendCnt(0);

                MerConfiguration merConfDO = merconfMapper.selectMerConf(smsg.getSysId(), smsg.getMerId());

                //全局黑名单
                if (checkMerUrlFromVirgoBlackList(smsg)) return;

                //商户白名单
                if (checkMerUrlFromMerWhiteList(smsg, merConfDO)) return;

                //商户黑名单
                if (checkMerUrlFromMerBlackList(smsg, merConfDO)) return;


                // 如果关闭，则不分发，直接入库，状态为U
                if (merConfDO != null && merConfDO.isStopFlag()) {
                    smsg.setSendStat(StringValues.SEND_STAT_U);
                    dispatcherSendMsgMapper.insertSendMsg(smsg);
                    return;
                }

                // 入库
                smsg.setSendStat(StringValues.SEND_STAT_Q);
                dispatcherSendMsgMapper.insertSendMsg(smsg);
                // 分发到不同的队列

                String queueName = ZkConstant.QUEUE_PREFIX + smsg.getSysId() + "." + smsg.getMerId();

                if (!activeMQQueueMap.containsKey(queueName)) {
                    addQueueToDB(queueName);
                    addQueueToMap(queueName);
                    applyQueueAdded(queueName);
                }

                messageSender.dispatch(smsg);

            } catch (VirgoException e) {
                insertError(smsg, EnumUtils.DispatherErrorType.VirgoException.getCode(), dataJsonString);
                logger.error("Message validate error!", e);
            } catch (Exception e) {
                logger.error("WaitDispatcherListener error", e);
            }
        }

        public boolean checkMerUrlFromMerBlackList(SendMsg smsg, MerConfiguration merConfDO) {
            if (merConfDO != null) {
                String urlblacklist = merConfDO.getUrlblacklist();
                String urlWhitelist = merConfDO.getUrlwhitelist();
                if (StringUtils.isNotBlank(urlblacklist) && StringUtils.isBlank(urlWhitelist)) {
                    if (CheckUtils.isList(urlblacklist, smsg.getUrl())) {
                        logger.warn("商户{}的请求{}连接{}在商户黑名单中{}", smsg.getMerId(), smsg.getOrdId(), smsg.getUrl(), urlblacklist);
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean checkMerUrlFromVirgoBlackList(SendMsg smsg) {
            VirgoConfiguration virgoConfiguration = virgoconfBiz.selectOneVirgoConfFromCache(1);
            if (virgoConfiguration != null && StringUtils.isNotBlank(virgoConfiguration.getBlackList())) {
                if (CheckUtils.isList(virgoConfiguration.getBlackList(), smsg.getUrl())) {
                    logger.warn("商户{}的请求{}连接{}在全局黑名单中{}", smsg.getMerId(), smsg.getOrdId(), smsg.getUrl(), virgoConfiguration.getBlackList());
                    return true;
                }
            }
            return false;
        }

        public boolean checkMerUrlFromMerWhiteList(SendMsg smsg, MerConfiguration merConfDO) {

            if (merConfDO != null) {
                String urlWhitelist = merConfDO.getUrlwhitelist();
                if (StringUtils.isNotBlank(urlWhitelist)) {
                    if (!CheckUtils.isList(urlWhitelist, smsg.getUrl())) {
                        logger.warn("商户{}的请求{}连接{}不在商户白名单中{}", smsg.getMerId(), smsg.getOrdId(), smsg.getUrl(), urlWhitelist);
                        return true;
                    }
                }
            }
            return false;
        }

        private void insertError(SendMsg smsg, int errorCode, String errorJson) {
            smsg.setErrorType(EnumUtils.DispatherErrorType.VirgoException.getCode());
            smsg.setErrorJson(errorJson);
            smsg.setErrorCreateDate(new Date());
            dispatcherSendMsgMapper.insertErrorSendMsg(smsg);
        }


    }

}
