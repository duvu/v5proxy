package com.v5project.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.stereotype.Component;

/**
 * @author beou on 11/20/17 00:20
 */
@Component
public class ProxyManager {
    String REMOTE_HOST = "127.0.0.1";
    String REMOTE_HOST2 = "127.0.0.1";
    int REMOTE_PORT = 31272;
    int REMOTE_PORT2 = 31273;

    int LOCAL_PORT = 31271;

    public void init() throws InterruptedException {


        // Configure the bootstrap.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new DuplicatorProxyInitializer(REMOTE_HOST, REMOTE_PORT,REMOTE_HOST2,REMOTE_PORT2))
                    .childOption(ChannelOption.AUTO_READ, false)
                    .bind(LOCAL_PORT).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
