/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.v5project.proxy.tcp;

import com.v5project.proxy.DiscardServerHandler;
import com.v5project.proxy.EventLoopFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpFrontendHandler extends ChannelInboundHandlerAdapter {

    private final String remoteHost;
    private final int remotePort;

    private final String remoteHost2;
    private final int remotePort2;

    // As we use inboundChannel.eventLoop() when buildling the Bootstrap this does not need to be volatile as
    // the server2OutboundChannel will use the same EventLoop (and therefore Thread) as the inboundChannel.
    private Channel server2OutboundChannel;
    private Channel server3OutboundChannel;
    Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    // TODO You should change this to your own executor
    private ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public TcpFrontendHandler(String remoteHost, int remotePort, String remoteHost2, int remotePort2) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.remoteHost2 = remoteHost2;
        this.remotePort2 = remotePort2;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel inboundChannel = ctx.channel();

        Bootstrap fwd1 = new Bootstrap();
        fwd1.group(inboundChannel.eventLoop())
                .channel(ctx.channel().getClass())
                .handler(new TcpBackendHandler(inboundChannel))
                .option(ChannelOption.AUTO_READ, false);
        ChannelFuture server2Future = fwd1.connect(remoteHost, remotePort);

        server2OutboundChannel = server2Future.channel();
        server2Future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    // connection complete start to read first data
                    inboundChannel.read();
                } else {
                    // Close the connection if the connection attempt has failed.
                    inboundChannel.close();
                }
            }
        });

        // Start the connection attempt to SERVER 3
        Bootstrap fwd2 = new Bootstrap();
        fwd2.group(EventLoopFactory.getWorker2())
                .channel(ctx.channel().getClass())
                .handler(new DiscardServerHandler()) // EDIT
                .option(ChannelOption.AUTO_READ, false);
        ChannelFuture server3Future = fwd2.connect(remoteHost2, remotePort2);
        server3OutboundChannel = server3Future.channel();
        //System.out.println("High Water Mark" + server3OutboundChannel.config().getWriteBufferHighWaterMark());
        // Here we are going to add channels to channel group to save bytebuf work
        channels.add(server2OutboundChannel);
        channels.add(server3OutboundChannel);
    }

    // You can keep this the same below or use the commented out section
    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object buf) {
        // You need to reference count the message +1
        ByteBuf msg  = (ByteBuf)buf;
        msg.retain();
        writeToChannel(ctx, buf, server2OutboundChannel);
        writeToChannel(ctx, buf, server3OutboundChannel);
    }

    private void writeToChannel(final ChannelHandlerContext ctx, Object msg, final Channel channel) {
        if (channel.isActive()) {
            channel.writeAndFlush(msg).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        LOGGER.info(String.format("Sent to %s", channel.remoteAddress().toString()));
                        ctx.channel().read();
                    } else {
                        future.channel().close();
                    }
                }
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (server2OutboundChannel != null) {
            closeOnFlush(server2OutboundChannel);
        }
        if (server3OutboundChannel != null) {
            closeOnFlush(server3OutboundChannel);
        }


        // Optionally can do this
//        channels.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        closeOnFlush(ctx.channel());
    }

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}