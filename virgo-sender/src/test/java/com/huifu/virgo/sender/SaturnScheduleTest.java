package com.huifu.virgo.sender;

import java.util.Timer;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ScheduledMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaturnScheduleTest {

	private static Logger LOG = LoggerFactory
			.getLogger(SaturnScheduleTest.class);

	private ActiveMQConnectionFactory factory;
	private Connection connection;
	private Session session;
	private Destination lockDestination;
	private MessageConsumer lockConsumer;
	private String lockQueueName = "saturn.sched.distributed.lock";
	private boolean active;
	private Timer timer = new Timer();
	private String[] tasks = new String[] { "USS_T1", "USS_T2" };

	public void start() throws JMSException {
		this.factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
		this.connection = this.factory.createConnection();
		this.connection.start();
		this.session = this.connection.createSession(false,
				Session.AUTO_ACKNOWLEDGE);
		this.lockDestination = this.session.createQueue(this.lockQueueName
				+ "?consumer.exclusive=true");

		// timer.schedule(new TryLockTask(), 1000, 1000);

		// send one message after start
		Message message = session.createMessage();
		MessageProducer producer = session.createProducer(lockDestination);
		producer.send(message);

		lockConsumer = this.session.createConsumer(lockDestination);
		Message startmsg = lockConsumer.receive();
		LOG.info("get lock");
		startProducing();
		// only receive one message
		lockConsumer.close();
		LOG.info("release lock");
	}

	private void startProducing() throws JMSException {

		Destination management = session
				.createTopic(ScheduledMessage.AMQ_SCHEDULER_MANAGEMENT_DESTINATION);
		Destination browseDest = session.createTemporaryQueue();

		for (String tname : tasks) {

			// Send the browse request
			MessageProducer mgtproducer = session.createProducer(management);
			Message request = session.createMessage();
			request.setStringProperty(ScheduledMessage.AMQ_SCHEDULER_ACTION,
					ScheduledMessage.AMQ_SCHEDULER_ACTION_BROWSE);
			// request.setStringProperty(ScheduledMessage.AMQ_SCHEDULER_ACTION_START_TIME,
			// Long.toString(start));
			// request.setStringProperty(ScheduledMessage.AMQ_SCHEDULER_ACTION_END_TIME,
			// Long.toString(end));
			request.setJMSReplyTo(browseDest);
			mgtproducer.send(request);

			// Create the "Browser"
			MessageConsumer browser = session.createConsumer(browseDest,
					"APP_TASK = '" + tname + "'");

			Message existJob = browser.receive(3000);
			if(existJob !=null){
				LOG.info("Removed schedule job {}", tname);
				Message rmvrequest = session.createMessage();
				rmvrequest.setStringProperty(ScheduledMessage.AMQ_SCHEDULER_ACTION,
						ScheduledMessage.AMQ_SCHEDULER_ACTION_REMOVE);
				rmvrequest.setStringProperty(ScheduledMessage.AMQ_SCHEDULED_ID,
						existJob.getStringProperty("scheduledJobId"));
				mgtproducer.send(rmvrequest);
			}
			mgtproducer.close();

			// create new schedule
			Destination schedQueue = session.createQueue(tname);
			MessageProducer jobproducer = session.createProducer(schedQueue);
			TextMessage message = session.createTextMessage("test msg");
			message.setStringProperty("APP_TASK", tname);

			 message.setStringProperty(ScheduledMessage.AMQ_SCHEDULED_CRON,
			 "15 * * * *");
//			message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 1000);
//			message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_PERIOD,
//					10000);
//			message.setIntProperty(ScheduledMessage.AMQ_SCHEDULED_REPEAT, 20);
			jobproducer.send(message);
			jobproducer.close();
			LOG.info("Start schedule job {}", tname);

		}
	}

	/**
	 * @param args
	 * @throws JMSException
	 */
	public static void main(String[] args) throws JMSException {
		SaturnScheduleTest ss = new SaturnScheduleTest();
		ss.start();
	}

}
