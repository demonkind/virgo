package com.huifu.virgo.sender;


import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ScheduledMessage;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleMessageTest {
	
	private static Logger LOG = LoggerFactory.getLogger(ScheduleMessageTest.class);
	
	@Test
	@Ignore
	public void testSchedMsg() throws Exception{
		// activemq.xml <broker ... schedulerSupport="true">
		 final int COUNT = 5;
		final CountDownLatch latch = new CountDownLatch(COUNT);
		
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(
				"tcp://localhost:61616");
		Connection conn = factory.createConnection();
		conn.start();
		Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination destination = session.createQueue("test-1");
		
		MessageConsumer consumer = session.createConsumer(destination);
		consumer.setMessageListener(new MessageListener(){
			@Override
			public void onMessage(Message message) {
				TextMessage tmsg = (TextMessage)message;
				try {
					LOG.info(tmsg.getText());
				} catch (JMSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				latch.countDown();
			}
			
		});
		
		MessageProducer producer = session.createProducer(destination);
		for(int i=1;i<COUNT+1;i++){
			TextMessage message = session.createTextMessage("test msg " +i);
			//message.setStringProperty(ScheduledMessage.AMQ_SCHEDULED_CRON, "0 * * * *");
			message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 500000);
//			message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_PERIOD, 1000);
//			message.setIntProperty(ScheduledMessage.AMQ_SCHEDULED_REPEAT, 2);
			LOG.info("send message "+i);
			producer.send(message);
//			session2.commit();
		}
		
		Destination management = session.createTopic(ScheduledMessage.AMQ_SCHEDULER_MANAGEMENT_DESTINATION);
		Destination browseDest = session.createTemporaryQueue();
		// Create the "Browser"
        MessageConsumer browser = session.createConsumer(browseDest);
        browser.setMessageListener(new MessageListener() {
            public void onMessage(Message message) {
            	LOG.info("Scheduled Message Browser got Message: " + message);
            }
        });
		// Send the remove request
        MessageProducer mgtproducer = session.createProducer(management);
        Message request = session.createMessage();
        request.setStringProperty(ScheduledMessage.AMQ_SCHEDULER_ACTION,
        					      ScheduledMessage.AMQ_SCHEDULER_ACTION_BROWSE);
//        request.setStringProperty(ScheduledMessage.AMQ_SCHEDULER_ACTION_START_TIME, Long.toString(start));
//        request.setStringProperty(ScheduledMessage.AMQ_SCHEDULER_ACTION_END_TIME, Long.toString(end));
        request.setJMSReplyTo(browseDest);
        mgtproducer.send(request);
		
		latch.await(10, TimeUnit.SECONDS);
        assertEquals(latch.getCount(), 0);
		
	}

}
