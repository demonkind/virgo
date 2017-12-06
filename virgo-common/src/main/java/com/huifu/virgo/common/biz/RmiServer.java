package com.huifu.virgo.common.biz;

import mx4j.tools.naming.NamingService;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

public class RmiServer {

    private MBeanServer mBeanServer;
    private JMXConnectorServer connectorServer;
    private JMXServiceURL jmxUrl;

    public RmiServer() throws Exception{
        mBeanServer = MBeanServerFactory.createMBeanServer();
        createRmiregistry();
        CreateJMXConnector();
    }
    public static void main(String[] args) throws Exception
    {

        RmiServer rmiServer = new RmiServer();
// 加一个测试MBean Hello对象
        rmiServer.startJMXConnector();

        System.out.println("Server up and running");
    }

    public ObjectName addMbean(Object o, String domain, String key, String value) throws Exception{
        ObjectName oName = new ObjectName(domain, key, value);
        mBeanServer.registerMBean(o, oName);
        return oName;
    }


    public void createRmiregistry() throws Exception{

// 1.注册NamingService MBean
        ObjectName namingName = ObjectName
                .getInstance("naming:type=rmiregistry");
// 这里采用MC4J的mx4j.tools.naming.NamingService
        NamingService ns = new NamingService();
        ns.setPort(8080);
        mBeanServer.registerMBean(ns, namingName);
// 2.启动NamingService MBean
        mBeanServer.invoke(namingName, "start", null, null);
    }


    public void CreateJMXConnector() throws Exception{// 1.nammingPort,从NamingService获得Port,缺省是1099
        ObjectName namingName = ObjectName.getInstance("naming:type=rmiregistry");
        int namingPort = ((Integer)mBeanServer.getAttribute(namingName, "Port")).intValue();

// 2. jndiPath
        String jndiPath = "/jmxconnector";
// 3. JMXServiceURL ,为:
// service:jmx:rmi://localhost/jndi/rmi://localhost:1099/jmxconnector
        jmxUrl = new JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:"
                + namingPort + jndiPath);

// 4.Create and start the RMIConnectorServer
// 中间设为null的参数,是针对认证的,我们这里没打开,设为null
        connectorServer = JMXConnectorServerFactory
                .newJMXConnectorServer(jmxUrl, null, mBeanServer);
    }

    public void startJMXConnector() throws Exception{
        connectorServer.start();
    }

    public void stopJMXConnector() throws Exception{
        connectorServer.stop();
    }


}
