package com.huifu.virgo.mock;

import com.huifu.virgo.remote.model.MerchantNotifyMessage;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * 
 * @author
 */
@Ignore
public class PerfSendMessageTest {

	private static Logger LOG = LoggerFactory.getLogger(PerfSendMessageTest.class);

	private ObjectMapper jsonMapper = new ObjectMapper();
	
	private AtomicInteger seqId = new AtomicInteger(0);

	private JmsTemplate jmsTemplate;

	private int sendCount = 10;
	
	@Test
	public void testSend() throws Exception{
		send();
	}
	
	public void send() throws Exception{
		ApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "classpath*:applicationContext-test2.xml" });

		String[] testurls = new String[] {
				"http://tech.chinapnr.com/hftest/page/BgRetUrl1101.jsp",
				"http://www.facebook.com/", 
				"http://www.sohu.com/",
//				"http://www.sina.com/", 
				"http://activemq.apache.org/",
				"https://github.com/"
				};

		jmsTemplate = (JmsTemplate) context.getBean("jmsTemplate");

		ExecutorService executor = Executors.newFixedThreadPool(5);
		CompletionService<String> compService = new ExecutorCompletionService<String>(
				executor);
		
		String postData = "MerId=880001&OrdId=115706&TransAmt=0.01&TransType=P&TransStat=S&MerPriv=This is a Test Private Data used by Merchant&GateId=09&SysDate=20090813&SysSeqId=039011&MerDate=20090813&ChkValue=CAD1A36856559E9D72D142D779FDF939205E7BBE1A0EE78844BA92E9A489A293EFC072D7BF9D18B8437494E20D27F5A29C481613B672D6F01F66B1BCACE69D2C84C041DB90F3C116ACACD068C0BA89984D0ECD3FF7297D9910D3B26CBA7177C33377F708A370D0A892D153DBD3079D63A2FFCEC3CE7BA7B86A4A51AD15FE9ED3";

		for (int i = 0; i < testurls.length; i++) {
			compService.submit(new SendThread("p2p", "" + i, testurls[i],
					postData));
		}

		for (int i = 0; i < testurls.length; i++) {
			Future<String> future = compService.take();
			String result = future.get();
			LOG.info(result);
		}
	}

	public class SendThread implements Callable<String> {

		String sysId;
		String merId;
		String url;
		String postData;

		String queueName;

		public SendThread(String sysId, String merId, String url,
				String postData) {
			this.sysId = sysId;
			this.merId = merId;
			this.url = url;
			this.postData = postData;

			this.queueName = "merchant.notify.service";
		}

		@Override
		public String call() throws Exception {
			for (int j = 0; j < sendCount; j++) {
				jmsTemplate.send(this.queueName, new MessageCreator() {
					public Message createMessage(Session session)
							throws JMSException {
						MerchantNotifyMessage smsg = new MerchantNotifyMessage();
						smsg.setSysId(sysId);
						smsg.setMerId(merId);
						smsg.setSysTxnId(""+seqId.incrementAndGet());
						smsg.setSendDate("20141208");
						smsg.setTransStat("S");
						smsg.setOrdId("115706");
						smsg.setUrl(url);
						smsg.setPostData(postData);
						String userDataJSON = null;
						try {
							userDataJSON = jsonMapper
									.writerWithDefaultPrettyPrinter().writeValueAsString(
											smsg);
						} catch (JsonGenerationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (JsonMappingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						TextMessage tm = session.createTextMessage();
						//tm.setStringProperty(StringValues.GSEND_ID,""+smsg.getId());
						tm.setText(userDataJSON);
						return tm;
					}
				});

			}
			return "Thread Done :" + this.sysId + " " + this.merId + " "
					+ this.url;
		}

	}
}
