package com.xx_dev.apn.proxy;

import org.apache.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * @author xmx
 * @version $Id: ApnProxySSLContextFactory.java, v 0.1 2013-3-26 上午11:22:10 xmx Exp $
 */
public class ApnProxySSLContextFactory {

    private static final Logger logger = Logger.getLogger(ApnProxySSLContextFactory.class);


    public static SSLEngine createClientSSLEnginForRemoteAddress(String host, int port) {
        try {
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = null;
            if (ApnProxyXmlConfig.getConfig().isUseTrustStore()) {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                KeyStore tks = KeyStore.getInstance("JKS");
                tks.load(new FileInputStream(ApnProxyXmlConfig.getConfig().getTrustStorePath()), ApnProxyXmlConfig.getConfig().getTrustStorePassword().toCharArray());
                tmf.init(tks);
                trustManagers = tmf.getTrustManagers();
            }


            sslcontext.init(null, trustManagers, null);

            return sslcontext.createSSLEngine(host, port);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static SSLEngine createServerSSLSSLEngine() {

        try {
            SSLContext sslcontext = SSLContext.getInstance("TLS");

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

            KeyStore ks = KeyStore.getInstance("JKS");
            KeyStore tks = KeyStore.getInstance("JKS");

            String keyStorePath = ApnProxyXmlConfig.getConfig().getKeyStorePath();
            String keyStorePassword = ApnProxyXmlConfig.getConfig().getKeyStroePassword();

            String trustStorePath = ApnProxyXmlConfig.getConfig().getTrustStorePath();
            String trustStorePassword = ApnProxyXmlConfig.getConfig().getKeyStroePassword();

            ks.load(new FileInputStream(keyStorePath), keyStorePassword.toCharArray());
            tks.load(new FileInputStream(trustStorePath), trustStorePassword.toCharArray());

            String keyPassword = ApnProxyXmlConfig.getConfig().getKeyStroePassword();
            kmf.init(ks, keyPassword.toCharArray());
            tmf.init(tks);

            sslcontext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            SSLEngine sslEngine = sslcontext.createSSLEngine();
            sslEngine.setUseClientMode(false);
            sslEngine.setNeedClientAuth(false); //should config?

            return sslcontext.createSSLEngine();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;

    }


}
