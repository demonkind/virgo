package worker.assign;

import com.huifu.virgo.common.base.ZkConstant;
import com.huifu.virgo.common.utils.ActiveMqJmxManager;
import com.huifu.virgo.common.utils.ActiveMqQueueBean;
import com.huifu.virgo.remote.model.MerConfiguration;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MqMbeanMonitor {

    private static Logger LOG = LoggerFactory.getLogger(MqMbeanMonitor.class);

    private CuratorFramework client = null;
    private ActiveMqJmxManager mqJmxMgr = null;

    private ObjectMapper jsonMapper = new ObjectMapper();

    public MqMbeanMonitor(CuratorFramework client) {
        this.client = client;
        this.mqJmxMgr = ActiveMqJmxManager.getInstance();
    }

    public void startup() throws Exception {
        ScheduledExecutorService executorService = Executors
                .newScheduledThreadPool(2);
        executorService.scheduleAtFixedRate(new QueueSizeMonitorTask(), 5, ZkConstant.ScaleupInterval,
                TimeUnit.SECONDS);
    }

    public void shutdown() throws Exception {
        mqJmxMgr.shutdown();
    }

    private class QueueSizeMonitorTask implements Runnable {

        @Override
        public void run() {
            // adjust queue consumer count by queue size and response time
            // statistic
            // org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=gsend.5.5
            try {
                Map<String, ActiveMqQueueBean> queueSizeMap = mqJmxMgr.listQueueBean();
                for (String queueName : queueSizeMap.keySet()) {

                    ActiveMqQueueBean activeMqQueueBean = queueSizeMap.get(queueName);
                    int consumerCount = activeMqQueueBean.getConsumerCount();
                    int queueSize = activeMqQueueBean.getQueueSize();
                    MerConfiguration merConf = jsonMapper.readValue(
                            new String(client.getData().forPath(
                                    ZkConstant.QUEUE_PATH + "/" + queueName)),
                            MerConfiguration.class);
//                    if (consumerCount == 0 && queueSize > 0) {
//                        String merconfStr = jsonMapper
//                                .writerWithDefaultPrettyPrinter()
//                                .writeValueAsString(merConf);
//                        client.setData().forPath(
//                                ZkConstant.QUEUE_PATH + "/" + queueName,
//                                merconfStr.getBytes());
//                        LOG.info("Queue adjust,Queue {},consumerCount:{},queueSize:{}", queueName, consumerCount, queueSize);
//                    }

                    if (queueSize > ZkConstant.ScaleupThreshold) {
                        if (!"H".equals(merConf.getCurrentStatus())) {
                            merConf.setCurrentStatus("H");
                            String merconfStr = jsonMapper
                                    .writerWithDefaultPrettyPrinter()
                                    .writeValueAsString(merConf);
                            client.setData().forPath(
                                    ZkConstant.QUEUE_PATH + "/" + queueName,
                                    merconfStr.getBytes());
                            LOG.info(
                                    "Queue {} scale up with max concurret consumer {}",
                                    queueName, merConf.getMaxConsumer());
                        }
                    } else {
                        if ("H".equals(merConf.getCurrentStatus())) {
                            merConf.setCurrentStatus("L");
                            String merconfStr = jsonMapper
                                    .writerWithDefaultPrettyPrinter()
                                    .writeValueAsString(merConf);
                            client.setData().forPath(
                                    ZkConstant.QUEUE_PATH + "/" + queueName,
                                    merconfStr.getBytes());
                            LOG.info(
                                    "Queue {} scale down with min concurret consumer {}",
                                    queueName, merConf.getMinConsumer());
                        }
                    }
                }
            } catch (Exception e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(
                ZkConstant.ConnectionString, new ExponentialBackoffRetry(1000,
                        3));
        client.start();
        MqMbeanMonitor mon = new MqMbeanMonitor(client);
        mon.startup();
    }

}
