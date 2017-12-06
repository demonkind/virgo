package com.huifu.virgo.sender;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MerHttpPostTest {
	private static Logger LOG = LoggerFactory.getLogger(MerHttpPostTest.class);

	@Test
	public void testInternalWebSite() throws Exception {
		int timeout = 3;
		CloseableHttpClient httpClient;
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(timeout * 1000)
				.setConnectionRequestTimeout(timeout * 1000)
				.setSocketTimeout(timeout * 1000).build();
		httpClient = HttpClientBuilder.create()
				.setDefaultRequestConfig(requestConfig).build();

		final HttpPost httppost = new HttpPost(
				"http://tech.chinapnr.com/hftest/page/BgRetUrl1101.jsp");
		httppost.setConfig(requestConfig);

		// add post data
		String postData = "MerId=880001&OrdId=115706&TransAmt=0.01&TransType=P&TransStat=S&MerPriv=This is a Test Private Data used by Merchant&GateId=09&SysDate=20090813&SysSeqId=039011&MerDate=20090813&ChkValue=CAD1A36856559E9D72D142D779FDF939205E7BBE1A0EE78844BA92E9A489A293EFC072D7BF9D18B8437494E20D27F5A29C481613B672D6F01F66B1BCACE69D2C84C041DB90F3C116ACACD068C0BA89984D0ECD3FF7297D9910D3B26CBA7177C33377F708A370D0A892D153DBD3079D63A2FFCEC3CE7BA7B86A4A51AD15FE9ED3";
		StringEntity reqEntity = new StringEntity(postData);
		reqEntity.setContentEncoding("UTF-8");
		reqEntity.setContentType("application/x-www-form-urlencoded");
		httppost.setEntity(reqEntity);

		// Create a custom response handler
		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
			public String handleResponse(final HttpResponse response)
					throws ClientProtocolException, IOException {
				int status = response.getStatusLine().getStatusCode();
				if (status >= 200 && status < 300) {
					HttpEntity entity = response.getEntity();
					// TODO parse response entity
					return entity != null ? EntityUtils.toString(entity) : null;
				} else {
					return "Unexpected response status: " + status;
				}
			}

		};
		
		String result = httpClient.execute(httppost, responseHandler);
		LOG.info(result);
		Assert.assertTrue(result.indexOf("RECV_ORD_ID_115706")>0);
	}

}
