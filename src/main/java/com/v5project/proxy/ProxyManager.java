package com.v5project.proxy;

import com.v5project.proxy.tcp.TcpProxyInitializer;
import com.v5project.proxy.udp.UdpProxyInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;

/**
 * @author beou on 11/20/17 00:20
 */
@Component
public class ProxyManager {
    public static final String  PROXY_CHANNEL_TYPES = "PROXY_CHANNEL_TYPES";
    public static final AttributeKey<ChannelType> PROXY_CHANNEL_TYPE = AttributeKey.valueOf(PROXY_CHANNEL_TYPES);

    private final ProxiesConfig proxiesConfig;
    //-- close later
    private final NioEventLoopGroup bossGroup;
    private final NioEventLoopGroup workerGroup;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public enum ChannelType {
        CHANNEL_TCP,
        CHANNEL_UDP
    }

    @Autowired
    public ProxyManager(ProxiesConfig proxiesConfig) {
        this.proxiesConfig = proxiesConfig;

        int bossNThread = proxiesConfig.getBossNThread() > 0 ? proxiesConfig.getBossNThread() : 2;
        int workerNThread = proxiesConfig.getWorkerNThread() > 0 ? proxiesConfig.getWorkerNThread() : 4;

        bossGroup = new NioEventLoopGroup(bossNThread);
        workerGroup = new NioEventLoopGroup(workerNThread);
    }

    public void init() throws InterruptedException {
        List<ProxiesConfig.Proxy> proxyList = proxiesConfig.getProxyList();

        LOGGER.info("Initiating ..." + proxyList.size());
        for (ProxiesConfig.Proxy proxy : proxyList) {
            if (!proxy.isEnabled()) {
                continue;
            }

            if (proxy.isDuplex()) {
                    ServerBootstrap b = new ServerBootstrap();
                    b.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .handler(new LoggingHandler(LogLevel.DEBUG))
                            .childHandler(new TcpProxyInitializer(proxy))
                            .childOption(ChannelOption.AUTO_READ, false)
                            .childAttr(PROXY_CHANNEL_TYPE, ChannelType.CHANNEL_TCP)
                            .bind(proxy.getPort());
            } else {
                    Bootstrap bootstrapping = new Bootstrap();
                    bootstrapping.group(bossGroup)
                            .channel(NioDatagramChannel.class)
                            .handler(new UdpProxyInitializer(proxy))
                            .attr(PROXY_CHANNEL_TYPE, ChannelType.CHANNEL_UDP)
                            .bind(proxy.getPort());
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
