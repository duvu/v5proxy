package com.v5project.proxy.config;

//import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author beou on 7/20/18 14:45
 */

public final class ConfigurationManager {
        private static final String PROXY_CONFIG = "proxies.xml";

    private static final Logger log = LoggerFactory.getLogger(ConfigurationManager.class);

    private static XMLConfiguration config;

    static {
        try {
            loadConfiguration();
        } catch (ConfigurationException e) {
            e.printStackTrace();
            log.warn("Failed to load", e);
        }
    }

    private static void loadConfiguration() throws ConfigurationException {
        log.info("Loading configuration");
        Configurations configs = new Configurations();
        config = configs.xml(PROXY_CONFIG);
    }

    public static int getThreadPoolBoss() {
        return config.getInt("proxies.thread-pool.boss", 2);
    }

    public static int getThreadPoolWorker() {
        return config.getInt("proxies.thread-pool.worker", 4);
    }

    public static int getThreadPoolWorkerUdp() {
        return config.getInt("proxies.thread-pool.worker-udp", 4);
    }

    public static List<ProxyEntry> getProxyList() {
        List<Object> objects = config.getList("proxies.proxy.port");
        if (objects != null) {
            List<ProxyEntry> list = new ArrayList<>();
            for (int i = 0; i < objects.size(); i++) {
                String key = "proxies.proxy("+ i +")";
                HierarchicalConfiguration<ImmutableNode> sub = config.configurationAt(key);
                ProxyEntry p = new ProxyEntry();
                p.setPort(sub.getInt("port"));
                p.setDuplex(sub.getBoolean("duplex"));
                p.setEnabled(sub.getBoolean("enabled"));
                p.setRemoteList(getRemoteList(sub));
                list.add(p);
            }
            return list;
        } else {
            return null;
        }
    }

    private static List<RemoteEndpoint> getRemoteList(HierarchicalConfiguration configuration) {
        int size = configuration.getList("remotes.remote.host").size();
        if (size > 0) {
            List<RemoteEndpoint> list = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                String key = "remotes.remote(" + i + ")";
                HierarchicalConfiguration<ImmutableNode> sub = configuration.configurationAt(key);
                RemoteEndpoint r = new RemoteEndpoint();
                r.setHost(sub.getString("host"));
                r.setPort(sub.getInt("port"));
                r.setTransparent(sub.getBoolean("transparent"));

                list.add(r);
            }
            return list;
        } else {
            return null;
        }
    }
}
