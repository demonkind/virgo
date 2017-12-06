package worker.assign;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.ConnectionFactory;

import org.apache.activemq.RedeliveryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.transaction.PlatformTransactionManager;

import com.huifu.virgo.common.base.ZkConstant;
import com.huifu.virgo.common.utils.ActiveMqJmxManager;
import com.huifu.virgo.worker.SpringMerQueueListener;

public class SpringSenderClient {

	private static Logger LOG = LoggerFactory.getLogger(SpringSenderClient.class);

	private String zkConnectionString;
	private CuratorFramework client;

	private PathChildrenCache queueCache = null;
	private NodeCache assignCache = null;

	private String zkPath;
	private ApplicationContext context;

	private String groupStr = "";
	private ConcurrentHashMap<String, DefaultMessageListenerContainer> queueMap = new ConcurrentHashMap<String, DefaultMessageListenerContainer>();

	public SpringSenderClient(String zkurl) {
		this.context = new ClassPathXmlApplicationContext(
				new String[] { "classpath*:applicationContext-test2.xml" });
		this.zkConnectionString = zkurl;
	}

	public void startup() throws Exception {

		client = CuratorFrameworkFactory.newClient(zkConnectionString,
				new ExponentialBackoffRetry(1000, 3));
		client.start();
		ZkConstant.makeZkPath(client);
		zkPath = client.create().creatingParentsIfNeeded()
				.withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
				.forPath(ZkConstant.SENDER_PATH + "/instance");

		queueCache = new PathChildrenCache(client, ZkConstant.QUEUE_PATH, true);
		queueCache.start();
		assignCache = new NodeCache(client, ZkConstant.ASSIGN_PATH, false);
		assignCache.start();
		LOG.info("Start sender instance : {}", zkPath);
		updateGroupStr(assignCache.getCurrentData());

		addZkQueueListener(queueCache);
		addZkAssignListener(assignCache);
	}

	private RedeliveryPolicy getRedeliveryPolicy(String qName) {
		RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
		redeliveryPolicy.setInitialRedeliveryDelay(0);
		redeliveryPolicy.setRedeliveryDelay(1000);
		redeliveryPolicy.setMaximumRedeliveries(6);
		redeliveryPolicy.setBackOffMultiplier((short) 2);
		redeliveryPolicy.setUseExponentialBackOff(true);
		return redeliveryPolicy;
	}

	protected void shutdown() throws Exception {
		CloseableUtils.closeQuietly(queueCache);
		CloseableUtils.closeQuietly(assignCache);
		CloseableUtils.closeQuietly(client);
	}

	private void addZkQueueListener(PathChildrenCache cache) {
		// a PathChildrenCacheListener is optional. Here, it's used just to log
		// changes
		PathChildrenCacheListener listener = new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework client,
					PathChildrenCacheEvent event) throws Exception {
				switch (event.getType()) {
				case CHILD_ADDED: {
					LOG.info("Node added: "
							+ ZKPaths
									.getNodeFromPath(event.getData().getPath()));
					applyQueueChange(event.getData(), true);
					break;
				}

				case CHILD_UPDATED: {
					LOG.info("Node changed: "
							+ ZKPaths
									.getNodeFromPath(event.getData().getPath()));
					applyQueueChange(event.getData(), true);
					break;
				}

				case CHILD_REMOVED: {
					LOG.info("Node removed: "
							+ ZKPaths
									.getNodeFromPath(event.getData().getPath()));
					applyQueueChange(event.getData(), false);
					break;
				}
				default:
					break;
				}
			}
		};
		cache.getListenable().addListener(listener);
	}

	private void addZkAssignListener(final NodeCache cache) {
		// a PathChildrenCacheListener is optional. Here, it's used just to log
		// changes
		NodeCacheListener listener = new NodeCacheListener() {
			@Override
			public void nodeChanged() throws Exception {
				ChildData currentData = cache.getCurrentData();
				applyAssignChange(currentData);
			}

		};
		cache.getListenable().addListener(listener);
	}

	private void applyAssignChange(ChildData currentData) throws Exception {
		updateGroupStr(currentData);
		if (groupStr == null || groupStr.length() == 0) {
			return;
		}
		// align the queue which belong to this sender
		for (ChildData queueNode : queueCache.getCurrentData()) {
			applyQueueChange(queueNode, true);
		}
	}

	private void updateGroupStr(ChildData currentData) throws Exception {
		String assigned = null;
		if (currentData != null) {
			assigned = new String(currentData.getData());
		} else {
			assigned = new String(client.getData().forPath(
					ZkConstant.ASSIGN_PATH));
		}
		if (assigned == null || assigned.length() == 0) {
			return;
		}
		LOG.info("Assign changed, apply now : {}", assigned);
		String[] slist = assigned.split(",");
		for (String sendera : slist) {
			if (sendera.startsWith(zkPath + " ")) {
				groupStr = sendera.substring(zkPath.length());
				break;
			}
		}
	}

	private void applyQueueChange(ChildData queueNode, boolean exist)
			throws Exception {
		String queueGrp = new String(queueNode.getData());
		String queueName = queueNode.getPath().substring(
				ZkConstant.QUEUE_PATH.length() + 1);
		if (groupStr.length() == 0) {
			return;
		}
		if (groupStr.indexOf(" " + queueGrp + " ") >= 0 && exist) {
			if (!queueMap.containsKey(queueName)) {
				// add queue consumer
				queueMap.put(queueName, createDMLC(queueName));
				LOG.info("Add queue : {}", queueName);
			}
		} else {
			// remove queue consumer
			if (queueMap.containsKey(queueName)) {
				queueMap.get(queueName).stop();
				queueMap.remove(queueName);
				// clean queue messages
		        ActiveMqJmxManager.getInstance().purgeQueue(queueName);
				LOG.info("Remove queue : {}", queueName);
			}
		}
	}

	private DefaultMessageListenerContainer createDMLC(
			String destinationName) throws Exception {
		AutowireCapableBeanFactory beanFactory = context
				.getAutowireCapableBeanFactory();

		DefaultMessageListenerContainer newBean = new DefaultMessageListenerContainer();
		// DefaultMessageListenerContainer newBean =
		// (DefaultMessageListenerContainer) beanFactory
		// .createBean(DefaultMessageListenerContainer.class,
		// AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
		// <property name="connectionFactory" ref="jmsFactory"/>
		// <property name="destination" ref="destination"/>
		// <property name="messageListener" ref="messageListener" />
		// <property name="pubSubDomain" value="true" />
		// <property name="cacheLevelName" value="CACHE_CONSUMER" />
		// <property name="sessionTransacted" value="true" />
		// <property name="transactionManager" ref="jmsTransactionManager" />
		// <property name="concurrentConsumers" value="5"/>

		newBean.setConnectionFactory((ConnectionFactory) context
				.getBean("connectionFactory"));
		newBean.setTransactionManager((PlatformTransactionManager) context
				.getBean("transactionManager"));
		newBean.setDestinationName(destinationName);
		newBean.setMessageListener(new SpringMerQueueListener());
		newBean.setCacheLevelName("CACHE_CONSUMER");
		newBean.setSessionTransacted(true);
		newBean.setPubSubDomain(false);
		newBean.setConcurrentConsumers(1);
		beanFactory.initializeBean(newBean, destinationName + "-lsnr");
		newBean.start();
		return newBean;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		SpringSenderClient sc = new SpringSenderClient(ZkConstant.ConnectionString);
		sc.startup();
		LOG.info("Press enter/return to quit");
		new BufferedReader(new InputStreamReader(System.in)).readLine();
		sc.shutdown();
		System.exit(0);
	}

}
