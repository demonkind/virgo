package com.huifu.virgo.sender;

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
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

/**
 * Created by jianfei.chen on 2015/1/21.
 */
public class MerHttpsPostTest {

    private CloseableHttpClient httpsClient;
    private RequestConfig requestConfig;
    private Logger logger = LoggerFactory.getLogger(MerHttpsPostTest.class);

    @Test
    public void merHttpsPostTest() {
        try {
            System.setProperty("jsse.enableSNIExtension", "false");
            getHttpsClientWithoutCheck();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            doPost("https://www.rongtonghuaxia.com");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void readMerUrl() {
        try {
            getHttpsClientWithoutCheck();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String filePath = "";
        try {
            filePath = new String(MerHttpsPostTest.class.getResource("/").getPath().getBytes(), "utf-8") + "virgo_url20160613.xlsx";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        HashMap<String, String> map = new HashMap<>();
        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream(filePath);
            Workbook wb = new XSSFWorkbook(inputStream);
            Sheet sheet = wb.getSheetAt(0);
            int totalRowNum = sheet.getLastRowNum();

            for (int i = 0; i < totalRowNum + 1; i++) {
                String merId = sheet.getRow(i).getCell(0).getStringCellValue();
                String url = sheet.getRow(i).getCell(1).getStringCellValue();

                String temp = url;
                if (temp.length() > 9) {
                    temp = temp.substring(9, temp.length());
                    int numIndex = (temp.indexOf("/") != -1) ? temp.indexOf("/") : temp.length();
                    url = url.substring(0, numIndex + 9);
                }

                if (!map.containsKey(url)) {
                    String returnMsg = null;
                    try {
                        int status = doPost(url);
                        returnMsg = String.valueOf(status);
                    } catch (Exception e) {
                        returnMsg = e.getLocalizedMessage();
                    }
//                    System.out.println("merId="+merId+";url="+url);
                    logger.info("merId={};url={};returnMsg={}", merId, url, returnMsg);
                    map.put(url, merId);
                }

            }
            wb.close();
        } catch (Exception e) {
            logger.error("error:", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    //IGNORE
                }
            }
        }

    }


    public int doPost(String url) throws IOException {
        int status = 0;
        String data = "";
        CloseableHttpResponse httpRsp = null;
        try {
            final HttpPost httppost = new HttpPost(url);
            httppost.setConfig(requestConfig);

            StringEntity reqEntity = new StringEntity(data);
            reqEntity.setContentEncoding("UTF-8");
            reqEntity.setContentType("application/x-www-form-urlencoded");
            httppost.setEntity(reqEntity);

            httpRsp = httpsClient.execute(httppost);

            status = httpRsp.getStatusLine().getStatusCode();


        } finally {
            if (httpRsp != null) {
                try {
                    httpRsp.close();
                } catch (IOException e) {
                    //IGNORE
                }
            }
        }

        return status;
    }

    public void getHttpsClientWithoutCheck() throws KeyManagementException, NoSuchAlgorithmException {

        requestConfig = RequestConfig.custom().setConnectionRequestTimeout(5 * 1000).setConnectTimeout(5 * 1000)
                .setSocketTimeout(5 * 1000).build();


        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create();
        ConnectionSocketFactory plainSF = new PlainConnectionSocketFactory();
        registryBuilder.register("http", plainSF);

        try {
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
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Registry<ConnectionSocketFactory> registry = registryBuilder.build();

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        cm.setDefaultMaxPerRoute(20);
        cm.setMaxTotal(200);


        httpsClient = HttpClientBuilder.create().setConnectionManager(cm).setDefaultRequestConfig(requestConfig).build();

    }


    public void getHttpsClient() throws KeyManagementException, NoSuchAlgorithmException {

        requestConfig = RequestConfig.custom().setConnectionRequestTimeout(5 * 1000).setConnectTimeout(5 * 1000)
                .setSocketTimeout(5 * 1000).build();

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();//创建connectionManager

        cm.setDefaultMaxPerRoute(20);//对每个指定连接的服务器（指定的ip）可以创建并发20 socket进行访问

        cm.setMaxTotal(200);

        SSLContext sslContext = SSLContext.getInstance("SSL");
        // set up a TrustManager that trusts everything
        sslContext.init(null, new TrustManager[]{new X509TrustManager() {

            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

            }

            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

            }

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }}, new SecureRandom());

        X509HostnameVerifier hostnameVerifier = new X509HostnameVerifier() {
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }

            public void verify(String host, SSLSocket ssl)
                    throws IOException {

            }

            public void verify(String host, X509Certificate cert)
                    throws SSLException {

            }

            public void verify(String host, String[] cns,
                               String[] subjectAlts) throws SSLException {
            }
        };
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        httpsClient = HttpClientBuilder.create().setConnectionManager(cm).setDefaultRequestConfig(requestConfig).setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext)).setSSLSocketFactory(socketFactory).build();
    }
}
