package com.huifu.virgo.common.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jianfei.chen on 2015/2/4.
 */
public class ZKConnectionStateListener implements ConnectionStateListener {
    private static Logger logger = LoggerFactory.getLogger(ZKConnectionStateListener.class);

    private ICallBack callBack;

    public ZKConnectionStateListener(ICallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {

        if (connectionState == ConnectionState.RECONNECTED) {
            logger.info("ConnectionState is RECONNECTED==========================================.");
            callBack.addReconnectedListener();
        }

        if (connectionState == ConnectionState.SUSPENDED) {
            logger.info("ConnectionState is SUSPENDED==========================================.");
        }

        if (connectionState == ConnectionState.LOST) {
            while (true) {
                logger.info("ConnectionState is LOST==========================================.");
                try {
                    if (curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                        callBack.addLostListener();
                        logger.info("LOST, zk path create listener again===================================.");
                        break;
                    }
                } catch (InterruptedException e) {
                    logger.error("stateChanged InterruptedException error+++++++++++++++++++++++++++++++", e);
                    break;
                } catch (Exception e) {
                    logger.error("stateChanged error+++++++++++++++++++++++++++++++", e);
                }
            }
        }
    }
}

