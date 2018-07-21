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
    //    String REMOTE_HOST = "127.0.0.1";
    //    String REMOTE_HOST2 = "dev.gpshandle.com";
    //    int REMOTE_PORT = 31271;
    //    int REMOTE_PORT2 = 31272;
    //    int LOCAL_PORT = 31272;

    private final ProxiesConfig proxiesConfig;
    //-- close later
    private final NioEventLoopGroup bossGroup;
    private final NioEventLoopGroup workerGroup;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

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
                            .handler(new LoggingHandler(LogLevel.INFO))
                            .childHandler(new TcpProxyInitializer(proxy))
                            .childOption(ChannelOption.AUTO_READ, false)
                            .bind(proxy.getPort());
            } else {
                    Bootstrap bootstrapping = new Bootstrap();
                    bootstrapping.group(bossGroup)
                            .channel(NioDatagramChannel.class)
                            .handler(new UdpProxyInitializer(proxy))
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
