package com.v5project.proxy;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

/**
 * @author beou on 7/20/18 14:45
 */

@Getter @Setter
@Configuration
@PropertySource("classpath:proxies-config.properties")
@ConfigurationProperties
public class ProxiesConfig {
    private int bossNThread;
    private int workerNThread;
    private List<Proxy> proxyList;

    @Getter @Setter
    public static class Proxy {
        private int port;
        private boolean duplex;
        private boolean enabled;
        private List<Remote> remoteList;
    }

    @Getter @Setter
    public static class Remote {
        private String host;
        private int port;
    }
}
