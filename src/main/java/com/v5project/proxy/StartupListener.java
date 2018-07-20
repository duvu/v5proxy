package com.v5project.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author beou on 7/20/18 14:54
 */

@Component
public class StartupListener implements ApplicationListener<ApplicationReadyEvent> {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final ProxyManager proxyManager;

    public StartupListener(ProxyManager proxyManager) {
        this.proxyManager = proxyManager;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        LOGGER.info("[>_] Proxy is ready ...");
        try {
            proxyManager.init();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
