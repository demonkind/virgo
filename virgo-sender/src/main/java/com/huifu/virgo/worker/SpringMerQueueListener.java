package com.huifu.virgo.worker;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huifu.virgo.remote.model.SendMsg;

public class SpringMerQueueListener implements MessageListener {

	private static Logger LOG = LoggerFactory.getLogger(SpringMerQueueListener.class);

	int timeout = 3;

	private ObjectMapper mapper = new ObjectMapper();

	private CloseableHttpClient httpClient;

	private CloseableHttpClient httpsClient;

	private RequestConfig requestConfig;

	public SpringMerQueueListener() throws Exception {
		
		requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(timeout * 1000)
				.setConnectTimeout(timeout * 1000)
				.setSocketTimeout(timeout * 1000).build();
		
		httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig)
				.build();

		SSLContext sslContext = SSLContext.getInstance("SSL");
		// set up a TrustManager that trusts everything
		sslContext.init(null, new TrustManager[] { new X509TrustManager() {
			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {
				// TODO Auto-generated method stub

			}

			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {
				// TODO Auto-generated method stub

			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				// TODO Auto-generated method stub
				return null;
			}
		} }, new SecureRandom());

		httpsClient = HttpClientBuilder
				.create()
				.setDefaultRequestConfig(requestConfig)
				.setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext))
				.build();

	}

	@SuppressWarnings("unused")
	private void close() {
		try {
			httpClient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			httpsClient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(Message message) {
		SendMsg msg = null;
		try {
			String userDataJSON = ((TextMessage) message).getText();
			msg = mapper.readValue(userDataJSON, SendMsg.class);

			String result = null;
			if (!checkURL(msg.getUrl())) {
				// drop message with invalid url
				result = "03";
			}
			result = httpPost(msg);
			// TODO result check( response status and RECV_ORD_ID_订单 ), 
			//if ok, update db and commit, else throw runtime exception 
			if(result.indexOf("RECV_ORD_ID_"+msg.getOrdId())>=0){
				throw new RuntimeException("Invalid merchant response");
			}
			LOG.info("Consumed {}", msg);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean checkURL(String url) {
		// TODO url filter
		return UrlValidator.getInstance().isValid(url);
	}

	private String httpPost(SendMsg msg) throws Exception {
		final HttpPost httppost = new HttpPost(msg.getUrl());
		httppost.setConfig(requestConfig);

		// add post data
		StringEntity reqEntity = new StringEntity(msg.getPostData());
		reqEntity.setContentEncoding("UTF-8");
		reqEntity.setContentType("application/x-www-form-urlencoded"); 
		httppost.setEntity(reqEntity);

		// System.out.println("Executing request " +
		// httpget.getRequestLine());

		// Create a custom response handler
		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
			public String handleResponse(final HttpResponse response)
					throws ClientProtocolException, IOException {
				int status = response.getStatusLine().getStatusCode();
				if (status >= 200 && status < 300) {
					HttpEntity entity = response.getEntity();
					//TODO parse response entity
					return entity != null ? EntityUtils.toString(entity) : null;
				} else {
					return "Unexpected response status: " + status;
				}
			}

		};

		if (msg.getUrl().startsWith("http://")) {
			return httpClient.execute(httppost, responseHandler);
		} else if (msg.getUrl().startsWith("https://")) {
			return httpsClient.execute(httppost, responseHandler);
		} else
			return "Unexpected schema";

	}

}
