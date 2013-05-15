package com.xx_dev.apn.proxy;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: ApnProxyConfig.java, v 0.1 2013-3-25 下午6:32:30 xmx Exp $
 */
public class ApnProxyConfig {
    private static Logger     logger = Logger.getLogger(ApnProxyConfig.class);

    private static Properties prop   = null;

    static {
        prop = new Properties();
        try {
            prop.load(new FileInputStream("conf/config.properties"));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static String getConfig(String configName) {
        return prop.getProperty(configName);
    }

}
