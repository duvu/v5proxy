package com.v5project.proxy;

import com.v5project.proxy.config.ConfigurationManager;
import com.v5project.proxy.config.ProxyEntry;
import com.v5project.proxy.tcp.TcpProxyInitializer;
import com.v5project.proxy.udp.UdpProxyInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.List;

/**
 * @author beou on 11/20/17 00:20
 */

public class ProxyManager {
    //-- close later
    private final NioEventLoopGroup bossGroup;
    private final NioEventLoopGroup workerGroup;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private static ProxyManager instance = null;

    public static ProxyManager getInstance() {
        if (instance == null) {
            instance = new ProxyManager();
        }
        return instance;
    }

    public ProxyManager() {
        int bossNThread = ConfigurationManager.getThreadPoolBoss();
        int workerNThread = ConfigurationManager.getThreadPoolWorker();

        bossGroup = new NioEventLoopGroup(bossNThread);
        workerGroup = new NioEventLoopGroup(workerNThread);
    }

    public void init() throws InterruptedException {
        List<ProxyEntry> proxyList = ConfigurationManager.getProxyList();

        LOGGER.info("Initiating ..." + proxyList.size());
        for (ProxyEntry proxy : proxyList) {
            if (!proxy.isEnabled()) {
                continue;
            }

            if (proxy.isDuplex()) {
                    ServerBootstrap b = new ServerBootstrap();
                    b.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            //.handler(new LoggingHandler(LogLevel.ERROR))
                            .childHandler(new TcpProxyInitializer(proxy))
                            .childOption(ChannelOption.AUTO_READ, true);
                Channel channel = b.bind(proxy.getPort()).sync().channel();

            } else {
                    Bootstrap b = new Bootstrap();
                    Channel channel = b.group(bossGroup)
                            .channel(NioDatagramChannel.class)
                            .handler(new UdpProxyInitializer(proxy))
                            .bind(proxy.getPort()).sync().channel();
            }
        }
    }

    @PreDestroy
    public void onDestroy() {
        LOGGER.info("Shutting down the proxy");
        try {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        } catch (Exception e) {
            LOGGER.error("Not able to shutdown the proxy", e);
        }
    }
}
