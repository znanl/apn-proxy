package com.xx_dev.apn.proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: ApnProxyXmlConfig.java, v 0.1 2013-6-1 上午1:32:48 xjx Exp $
 */
public class ApnProxyXmlConfig {

    private static final Logger       logger          = Logger.getLogger(ApnProxyXmlConfig.class);

    private static ApnProxyXmlConfig  config;

    private ApnProxyListenType        listenType;

    private String                    tripleDesKey;

    private String                    keyStorePath;

    private String                    keyStroePassword;

    private int                       port;

    private int                       bossThreadCount;

    private int                       workerThreadCount;

    private String                    pacHost;

    private boolean                   useIpV6;

    private List<ApnProxyRemoteRule>  remoteRuleList  = new ArrayList<ApnProxyRemoteRule>();

    private List<ApnProxyLocalIpRule> localIpRuleList = new ArrayList<ApnProxyLocalIpRule>();

    public void init() {
        Document doc = null;
        try {
            Builder parser = new Builder();
            doc = parser.build("conf/config.xml");
        } catch (ParsingException ex) {
            logger.error(ex.getMessage(), ex);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
        if (doc == null) {
            return;
        }
        Element rootElement = doc.getRootElement();

        Elements listenTypeElements = rootElement.getChildElements("listen-type");
        if (listenTypeElements.size() == 1) {
            String _listenType = listenTypeElements.get(0).getValue();
            if (StringUtils.equals(_listenType, "3des")) {
                this.listenType = ApnProxyListenType.TRIPLE_DES;
            } else if (StringUtils.equals(_listenType, "ssl")) {
                this.listenType = ApnProxyListenType.SSL;
            } else if (StringUtils.equals(_listenType, "plain")) {
                this.listenType = ApnProxyListenType.PLAIN;
            } else {
                throw new ApnProxyConfigException("Unknown listen type");
            }
        }

        Elements tripleDesKeyElements = rootElement.getChildElements("triple-des-key");
        if (tripleDesKeyElements.size() == 1) {
            this.tripleDesKey = tripleDesKeyElements.get(0).getValue();
        }

        Elements keyStoreElements = rootElement.getChildElements("key-store");
        if (keyStoreElements.size() == 1) {
            Elements keyStorePathElements = keyStoreElements.get(0).getChildElements("path");
            if (keyStorePathElements.size() == 1) {
                this.keyStorePath = keyStorePathElements.get(0).getValue();
            }
            Elements keyStorePasswordElements = keyStoreElements.get(0)
                .getChildElements("password");
            if (keyStorePasswordElements.size() == 1) {
                this.keyStroePassword = keyStorePasswordElements.get(0).getValue();
            }
        }

        Elements portElements = rootElement.getChildElements("port");
        if (tripleDesKeyElements.size() == 1) {
            try {
                this.port = Integer.parseInt(portElements.get(0).getValue());
            } catch (NumberFormatException nfe) {
                throw new ApnProxyConfigException("Invalid format for: port", nfe);
            }
        }

        Elements threadCountElements = rootElement.getChildElements("thread-count");
        if (threadCountElements.size() == 1) {
            Elements bossElements = threadCountElements.get(0).getChildElements("boss");
            if (bossElements.size() == 1) {
                try {
                    this.bossThreadCount = Integer.parseInt(bossElements.get(0).getValue());
                } catch (NumberFormatException nfe) {
                    throw new ApnProxyConfigException("Invalid format for: boss", nfe);
                }
            }
            Elements workerElements = threadCountElements.get(0).getChildElements("worker");
            if (workerElements.size() == 1) {
                try {
                    this.workerThreadCount = Integer.parseInt(workerElements.get(0).getValue());
                } catch (NumberFormatException nfe) {
                    throw new ApnProxyConfigException("Invalid format for: worker", nfe);
                }
            }
        }

        Elements pacHostElements = rootElement.getChildElements("pac-host");
        if (pacHostElements.size() == 1) {
            this.pacHost = pacHostElements.get(0).getValue();
        }

        Elements useIpv6Elements = rootElement.getChildElements("use-ipv6");
        if (useIpv6Elements.size() == 1) {
            this.useIpV6 = Boolean.parseBoolean(portElements.get(0).getValue());
        }

        Elements remoteRulesElements = rootElement.getChildElements("remote-rules");
        if (remoteRulesElements.size() == 1) {
            Elements ruleElements = remoteRulesElements.get(0).getChildElements("rule");

            for (int i = 0; i < ruleElements.size(); i++) {
                ApnProxyRemoteRule apnProxyRemoteRule = new ApnProxyRemoteRule();

                Element ruleElement = ruleElements.get(i);

                Elements remoteHostElements = ruleElement.getChildElements("remote-host");
                if (remoteHostElements.size() != 1) {
                    throw new ApnProxyConfigException("Wrong config for: remote-host");
                }
                String remoteHost = remoteHostElements.get(0).getValue();

                Elements remotePortElements = ruleElement.getChildElements("remote-port");
                if (remoteHostElements.size() != 1) {
                    throw new ApnProxyConfigException("Wrong config for: remote-port");
                }
                int remotePort = -1;
                try {
                    remotePort = Integer.parseInt(remotePortElements.get(0).getValue());
                } catch (NumberFormatException nfe) {
                    throw new ApnProxyConfigException("Invalid format for: remote-port", nfe);
                }

                apnProxyRemoteRule.setRemoteHost(remoteHost);
                apnProxyRemoteRule.setRemotePort(remotePort);

                Elements applyListElements = ruleElement.getChildElements("apply-list");
                if (applyListElements.size() == 1) {
                    Elements originalHostElements = applyListElements.get(0).getChildElements(
                        "original-host");

                    List<String> originalHostList = new ArrayList<String>();
                    for (int j = 0; j < originalHostElements.size(); j++) {
                        String originalHost = originalHostElements.get(j).getValue();
                        originalHostList.add(originalHost);
                    }
                    apnProxyRemoteRule.setOriginalHostList(originalHostList);
                }

                remoteRuleList.add(apnProxyRemoteRule);
            }
        }

        Elements localIpRulesElements = rootElement.getChildElements("local-ip-rules");
        if (localIpRulesElements.size() == 1) {
            Elements ruleElements = localIpRulesElements.get(0).getChildElements("rule");

            for (int i = 0; i < ruleElements.size(); i++) {
                ApnProxyLocalIpRule apnProxyLocalIpRule = new ApnProxyLocalIpRule();

                Element ruleElement = ruleElements.get(i);

                Elements localIpElements = ruleElement.getChildElements("local-ip");
                if (localIpElements.size() != 1) {
                    throw new ApnProxyConfigException("Wrong config for: local-ip");
                }
                String localIp = localIpElements.get(0).getValue();

                apnProxyLocalIpRule.setLocalIp(localIp);

                Elements applyListElements = ruleElement.getChildElements("apply-list");
                if (applyListElements.size() == 1) {
                    Elements originalHostElements = applyListElements.get(0).getChildElements(
                        "original-host");

                    List<String> originalHostList = new ArrayList<String>();
                    for (int j = 0; j < originalHostElements.size(); j++) {
                        String originalHost = originalHostElements.get(j).getValue();
                        originalHostList.add(originalHost);
                    }
                    apnProxyLocalIpRule.setOriginalHostList(originalHostList);
                }

                localIpRuleList.add(apnProxyLocalIpRule);
            }

        }

        config = this;

    }

    public final ApnProxyListenType getListenType() {
        return listenType;
    }

    public final void setListenType(ApnProxyListenType listenType) {
        this.listenType = listenType;
    }

    public final String getTripleDesKey() {
        return tripleDesKey;
    }

    public final void setTripleDesKey(String tripleDesKey) {
        this.tripleDesKey = tripleDesKey;
    }

    public final String getKeyStorePath() {
        return keyStorePath;
    }

    public final void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public final String getKeyStroePassword() {
        return keyStroePassword;
    }

    public final void setKeyStroePassword(String keyStroePassword) {
        this.keyStroePassword = keyStroePassword;
    }

    public final int getPort() {
        return port;
    }

    public final void setPort(int port) {
        this.port = port;
    }

    public final int getBossThreadCount() {
        return bossThreadCount;
    }

    public final void setBossThreadCount(int bossThreadCount) {
        this.bossThreadCount = bossThreadCount;
    }

    public final int getWorkerThreadCount() {
        return workerThreadCount;
    }

    public final void setWorkerThreadCount(int workerThreadCount) {
        this.workerThreadCount = workerThreadCount;
    }

    public final String getPacHost() {
        return pacHost;
    }

    public final void setPacHost(String pacHost) {
        this.pacHost = pacHost;
    }

    public final boolean isUseIpV6() {
        return useIpV6;
    }

    public final void setUseIpV6(boolean useIpV6) {
        this.useIpV6 = useIpV6;
    }

    public final List<ApnProxyRemoteRule> getRemoteRuleList() {
        return remoteRuleList;
    }

    public final void setRemoteRuleList(List<ApnProxyRemoteRule> remoteRuleList) {
        this.remoteRuleList = remoteRuleList;
    }

    public final List<ApnProxyLocalIpRule> getLocalIpRuleList() {
        return localIpRuleList;
    }

    public final void setLocalIpRuleList(List<ApnProxyLocalIpRule> localIpRuleList) {
        this.localIpRuleList = localIpRuleList;
    }

    public static ApnProxyXmlConfig getConfig() {
        return config;
    }

    public class ApnProxyRemoteRule {
        private String       remoteHost;
        private int          remotePort;
        private List<String> originalHostList;

        public final String getRemoteHost() {
            return remoteHost;
        }

        public final void setRemoteHost(String remoteHost) {
            this.remoteHost = remoteHost;
        }

        public final int getRemotePort() {
            return remotePort;
        }

        public final void setRemotePort(int remotePort) {
            this.remotePort = remotePort;
        }

        public final List<String> getOriginalHostList() {
            return originalHostList;
        }

        public final void setOriginalHostList(List<String> originalHostList) {
            this.originalHostList = originalHostList;
        }
    }

    public class ApnProxyLocalIpRule {
        private String       localIp;
        private List<String> originalHostList;

        public final String getLocalIp() {
            return localIp;
        }

        public final void setLocalIp(String localIp) {
            this.localIp = localIp;
        }

        public final List<String> getOriginalHostList() {
            return originalHostList;
        }

        public final void setOriginalHostList(List<String> originalHostList) {
            this.originalHostList = originalHostList;
        }

    }

    public enum ApnProxyListenType {
        TRIPLE_DES, SSL, PLAIN;
    }

    public class ApnProxyConfigException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ApnProxyConfigException(String msg) {
            super(msg);
        }

        public ApnProxyConfigException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}