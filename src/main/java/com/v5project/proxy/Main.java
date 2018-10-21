package com.v5project.proxy;

import com.v5project.proxy.config.ConfigurationManager;
import com.v5project.proxy.config.ProxyEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);
	public static void main(String[] args) {
//		int bossN = ConfigurationManager.getThreadPoolBoss();
		List<ProxyEntry> list = ConfigurationManager.getProxyList();
		log.info("Starting ProxyEntry: " + list.size());
		try {
			ProxyManager.getInstance().init();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
