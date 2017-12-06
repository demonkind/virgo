package com.huifu.virgo.worker;

import com.huifu.virgo.common.base.BaseUtils;
import com.huifu.virgo.common.base.StringValues;
import com.huifu.virgo.common.mapper.DlqMapper;
import com.huifu.virgo.remote.model.MerConfiguration;
import com.huifu.virgo.remote.model.SendMsg;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.jms.pool.PooledSession;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.DateValidator;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.net.ssl.SSLContext;
import java.io.Closeable;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

public class MerQueueListener implements MessageListener, Closeable {

    private static Logger LOG = LoggerFactory.getLogger(MerQueueListener.class);

    private ObjectMapper jsonMapper = new ObjectMapper();

    private CloseableHttpClient httpClient;

    private CloseableHttpClient httpsClient;

    private RequestConfig requestConfig;

    private MerConfiguration merConfig;

    private DlqMapper dlqMapper;

    private Session session;

    private DateValidator dateValidator = DateValidator.getInstance();

    public MerQueueListener(Session session, MerConfiguration merConf) throws Exception {
        PooledSession pooledSession = (PooledSession) session;
        this.session = (ActiveMQSession) pooledSession.getInternalSession();

        this.merConfig = merConf;

        requestConfig = RequestConfig.custom().setConnectionRequestTimeout(merConfig.getHttpTimeout() * 1000).setConnectTimeout(merConfig.getHttpTimeout() * 1000)
                .setSocketTimeout(merConfig.getHttpTimeout() * 1000).build();

        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create();
        ConnectionSocketFactory plainSF = new PlainConnectionSocketFactory();
        registryBuilder.register("http", plainSF);

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        SSLContext sslContext = SSLContexts.custom().useTLS().loadTrustMaterial(trustStore, new TrustStrategy() {
            //信任所有
            public boolean isTrusted(X509Certificate[] chain,
                                     String authType) throws CertificateException {
                return true;
            }
        }).build();
        LayeredConnectionSocketFactory sslSF = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        registryBuilder.register("https", sslSF);
        Registry<ConnectionSocketFactory> registry = registryBuilder.build();

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        cm.setDefaultMaxPerRoute(20);
        cm.setMaxTotal(200);

        httpClient = HttpClientBuilder.create().setConnectionManager(cm).setDefaultRequestConfig(requestConfig).build();
        httpsClient = HttpClientBuilder.create().setConnectionManager(cm).setDefaultRequestConfig(requestConfig).build();

    }

    public void setDlqMapper(DlqMapper dlqMapper) {
        this.dlqMapper = dlqMapper;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    public void close() {
        try {
            httpClient.close();
            httpsClient.close();
        } catch (Exception e) {
            LOG.info(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void onMessage(Message message) {
        SendMsg msg = null;
        String result = null;
        try {
            String userDataJSON = ((TextMessage) message).getText();
            msg = jsonMapper.readValue(userDataJSON, SendMsg.class);
            result = httpPost(msg);
        } catch (Exception e) {
            LOG.error("error:", e);
            result = e.getLocalizedMessage();
        }
        if (result.equals("OK")) {
            msg.setSendStat(StringValues.SEND_STAT_S);
            msg.setLastSendTime(dateValidator.format(new Date(), BaseUtils.SIMPLE_DATE_FORMAT));
            msg.setLastSendResult("OK");
            dlqMapper.updateSendMsg(msg);
        } else if ("NO_RECV_ORD_ID".equals(result)) {
            msg.setSendStat(StringValues.SEND_STAT_F);
            msg.setLastSendTime(dateValidator.format(new Date(), BaseUtils.SIMPLE_DATE_FORMAT));
            msg.setLastSendResult(StringUtils.abbreviate(result, 40));
            dlqMapper.updateSendMsg(msg);
        } else {
            msg.setSendStat(StringValues.SEND_STAT_F);
            msg.setLastSendTime(dateValidator.format(new Date(), BaseUtils.SIMPLE_DATE_FORMAT));
            msg.setLastSendResult(StringUtils.abbreviate(result, 40));
            dlqMapper.updateSendMsg(msg);
            throw new RuntimeException(result);
        }

    }

    private String httpPost(SendMsg msg) {
        LOG.info("request info,sysid:{},merid:{},orderid:{},url:{}", msg.getSysId(), msg.getMerId(), msg.getOrdId(), msg.getUrl());
        CloseableHttpResponse httpRsp = null;
        try {
            final HttpPost httppost = new HttpPost(msg.getUrl());
            httppost.setConfig(requestConfig);
            httppost.setHeader("Accept-Language", "zh-cn");

            // add post data
            StringEntity reqEntity = new StringEntity(msg.getPostData());

//            reqEntity.setContentEncoding("UTF-8");
            reqEntity.setContentType("application/x-www-form-urlencoded");
            httppost.setEntity(reqEntity);

            // System.out.println("Executing request " +
            // httpget.getRequestLine());


            if (msg.getUrl().startsWith("http://")) {
                httpRsp = httpClient.execute(httppost);
            } else if (msg.getUrl().startsWith("https://")) {
                httpRsp = httpsClient.execute(httppost);
            } else
                return "Unexpected schema";

            int status = httpRsp.getStatusLine().getStatusCode();

            LOG.info("response status:{},sysid:{},merid:{},orderid:{},url:{}", status,msg.getSysId(), msg.getMerId(), msg.getOrdId(), msg.getUrl());
            if (status >= 200 && status < 300) {
                HttpEntity entity = httpRsp.getEntity();
//                if (entity != null) {
//                    LOG.info("response HttpEntity is {}", EntityUtils.toString(entity));
//                } else {
//                    LOG.info("response HttpEntity is null");
//                }

                if (entity != null && EntityUtils.toString(entity).indexOf("RECV_ORD_ID_" + msg.getOrdId()) >= 0) {
                    return "OK";
                } else {
                    return "NO_RECV_ORD_ID";
                }
            } else {
                return "HTTP " + status;
            }
        } catch (Exception e) {
            LOG.info("Failed {},{},{},{},,{}", msg.getSysId(), msg.getMerId(), msg.getOrdId(), msg.getUrl(), e.getLocalizedMessage());
            return e.getLocalizedMessage();
        } finally {
            if (httpRsp != null) {
                try {
                    httpRsp.close();
                } catch (IOException e) {
                    //IGNORE
                }
            }
        }
    }

}
