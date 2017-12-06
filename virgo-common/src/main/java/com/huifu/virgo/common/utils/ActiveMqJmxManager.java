package com.huifu.virgo.common.utils;

import com.huifu.virgo.common.base.StringValues;
import com.huifu.virgo.common.base.ZkConstant;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActiveMqJmxManager {

    private static Logger LOG = LoggerFactory
            .getLogger(ActiveMqJmxManager.class);

    private static volatile ActiveMqJmxManager instance = null;

    private ActiveMqJmxManager() {
    }

    public static ActiveMqJmxManager getInstance() {
        if (instance == null) {
            synchronized (ActiveMqJmxManager.class) {
                if (instance == null) {
                    instance = new ActiveMqJmxManager();
                    instance.startup();
                }
            }
        }

        return instance;
    }

    private JMXConnector jmxConnector = null;

    private MBeanServerConnection mbeanConnection = null;

    private BrokerViewMBean brokerViewMBean = null;

    private int count = 0;

    public synchronized void startup() {
        try {
            String[] jmxParas = ZkConstant.MqJmxUrl.split(",");
            String[] mqhost = new String[2];
            String[] mqrmiport = new String[2];
            String[] username = new String[2];
            String[] password = new String[2];

            int number = 0;
            if (jmxParas.length == 4) {
                mqhost[0] = jmxParas[0];
                mqrmiport[0] = jmxParas[1];
                username[0] = jmxParas[2];
                password[0] = jmxParas[3];
                mqhost[1] = jmxParas[0];
                mqrmiport[1] = jmxParas[1];
                username[1] = jmxParas[2];
                password[1] = jmxParas[3];
            } else if (jmxParas.length == 8) {
                mqhost[0] = jmxParas[number++];
                mqrmiport[0] = jmxParas[number++];
                username[0] = jmxParas[number++];
                password[0] = jmxParas[number++];
                mqhost[1] = jmxParas[number++];
                mqrmiport[1] = jmxParas[number++];
                username[1] = jmxParas[number++];
                password[1] = jmxParas[number];
            }
            int jxmNumber = count % 2;

            Map<String, Object> env = new HashMap<String, Object>();
            String[] credentials = new String[]{username[jxmNumber], password[jxmNumber]};
            env.put("jmx.remote.credentials", credentials);

            JMXServiceURL url = new JMXServiceURL(
                    "service:jmx:rmi:///jndi/rmi://" + mqhost[jxmNumber] + ":" + mqrmiport[jxmNumber]
                            + "/jmxrmi");

            jmxConnector = JMXConnectorFactory.connect(url, env);
            mbeanConnection = jmxConnector.getMBeanServerConnection();

            ObjectInstance brokerON = mbeanConnection
                    .queryMBeans(
                            new ObjectName(
                                    "org.apache.activemq:type=Broker,brokerName=*"),
                            null).iterator().next();
            brokerViewMBean = MBeanServerInvocationHandler.newProxyInstance(
                    mbeanConnection, brokerON.getObjectName(),
                    BrokerViewMBean.class, false);
            LOG.info("Successfully connected to ActiveMQ MBean Server {}:{}", mqhost[jxmNumber], mqrmiport[jxmNumber]);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            count++;
            instance = null;
            startup();
        }
    }

    public void shutdown() {
        try {
            jmxConnector.close();
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    public void deleteQueueBean(String sysId, String merId) {
        try {
            ObjectName queueON = new ObjectName(
                    "org.apache.activemq:type=Broker,brokerName="
                            + brokerViewMBean.getBrokerName()
                            + ",destinationType=Queue,destinationName=" + ZkConstant.QUEUE_PREFIX + sysId + "." + merId);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            count++;
            instance = null;
            startup();
        }
    }

    /**
     * List all gsend queue and size map
     *
     * @throws Exception
     */
    public Map<String, ActiveMqQueueBean> listQueueBean() {
        Map<String, ActiveMqQueueBean> queueSizeMap = new HashMap<String, ActiveMqQueueBean>();
        try {
            ObjectName queueON = new ObjectName(
                    "org.apache.activemq:type=Broker,brokerName="
                            + brokerViewMBean.getBrokerName()
                            + ",destinationType=Queue,destinationName=" + ZkConstant.QUEUE_PREFIX + "*");

            for (ObjectInstance qBeanOI : mbeanConnection.queryMBeans(queueON,
                    null)) {
                String queueMbeanName = qBeanOI.getObjectName().toString();
                String queueName = queueMbeanName.substring(queueMbeanName
                        .indexOf("destinationName=") + 16);

//                LOG.info("qBeanOI.getObjectName={}", qBeanOI.getObjectName());

                Integer consumerCount = Integer.parseInt(mbeanConnection
                        .getAttribute(qBeanOI.getObjectName(), "ConsumerCount")
                        .toString());


                Integer queueSize = Integer.parseInt(mbeanConnection
                        .getAttribute(qBeanOI.getObjectName(), "QueueSize")
                        .toString());

//                LOG.info("queueName={},consumerCount={},queueSize={}", new Object[]{queueName, consumerCount, queueSize});

                ActiveMqQueueBean activeMqQueueBean = new ActiveMqQueueBean();
                activeMqQueueBean.setConsumerCount(consumerCount);
                activeMqQueueBean.setQueueSize(queueSize);

                queueSizeMap.put(queueName, activeMqQueueBean);
            }
            return queueSizeMap;
        } catch (Exception e) {
            // in case of jmx connection issue ,reestablish it
            LOG.error(e.getLocalizedMessage(), e);
            count++;
            instance = null;
            startup();
        }
        return queueSizeMap;

    }

    private QueueViewMBean getQueueViewMBean(String queueName)
            throws MalformedObjectNameException {

        ObjectName queueON = new ObjectName(
                "org.apache.activemq:type=Broker,brokerName="
                        + brokerViewMBean.getBrokerName()
                        + ",destinationType=Queue,destinationName=" + queueName);
        QueueViewMBean queueVMBean = (QueueViewMBean) MBeanServerInvocationHandler
                .newProxyInstance(mbeanConnection, queueON,
                        QueueViewMBean.class, false);
        return queueVMBean;

    }

    /**
     * Removes the messages matching the given GSEND_ID by selector
     *
     * @param queueName
     * @param msgIds
     * @throws Exception
     */
    public void removeMessageFromQueue(String queueName, List<String> msgIds) {
        try {
            QueueViewMBean queueVMBean = getQueueViewMBean(queueName);
            for (String mid : msgIds) {
                queueVMBean.removeMatchingMessages(StringValues.GSEND_ID + "='"
                        + mid + "'");
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            startup();
        }

    }

    /**
     * Removes all of the messages in the queue.
     *
     * @param queueName
     * @throws Exception
     */
    public void purgeQueue(String queueName) {
        try {
            QueueViewMBean queueVMBean = getQueueViewMBean(queueName);
            queueVMBean.purge();
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            startup();
        }
    }

}
