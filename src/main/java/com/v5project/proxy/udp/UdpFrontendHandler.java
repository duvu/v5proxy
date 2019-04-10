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
package com.v5project.proxy.udp;

import com.v5project.proxy.DiscardServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpFrontendHandler extends ChannelInboundHandlerAdapter {

    private final String remoteHost;
    private final int remotePort;

    private final String remoteHost2;
    private final int remotePort2;

    private Channel s2oChannel;
    private Channel s3oChannel;

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    public UdpFrontendHandler(String remoteHost, int remotePort, String remoteHost2, int remotePort2) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.remoteHost2 = remoteHost2;
        this.remotePort2 = remotePort2;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel inboundChannel = ctx.channel();

        // Start the connection attempt to SERVER 2
        Bootstrap server2Bootstrap = new Bootstrap();
        server2Bootstrap.group(inboundChannel.eventLoop())
                .channel(ctx.channel().getClass())
                .handler(new DiscardServerHandler())
                .option(ChannelOption.AUTO_READ, false);
        ChannelFuture server2Future = server2Bootstrap.connect(remoteHost, remotePort);

        s2oChannel = server2Future.channel();

        // Start the connection attempt to SERVER 3
        Bootstrap server3Bootstrap = new Bootstrap();
        server3Bootstrap.group(inboundChannel.eventLoop())
                .channel(ctx.channel().getClass())
                .handler(new DiscardServerHandler()) // EDIT
                .option(ChannelOption.AUTO_READ, false);
        ChannelFuture server3Future = server3Bootstrap.connect(remoteHost2, remotePort2);
        s3oChannel = server3Future.channel();
    }

    // You can keep this the same below or use the commented out section
    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object buf) {
        ByteBuf msg = null;
        if (buf instanceof DatagramPacket) {
            System.out.println("[>_] UDP DatagramPacket");
            msg = ((DatagramPacket) buf).content();
            msg.retain();
            forward(ctx, msg);
        } else {
            System.out.println("Not a DatagramPacket");
            try {
                msg  = (ByteBuf)buf;
                msg.retain();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                forward(ctx, msg);
            }
        }
    }

    private void forward(final ChannelHandlerContext ctx, ByteBuf msg) {

        forwardToChannel(ctx, msg, s2oChannel);
        forwardToChannel(ctx, msg, s3oChannel);
    }

    private void forwardToChannel(final ChannelHandlerContext ctx, ByteBuf msg, final Channel channel) {
        if (channel.isActive()) {
            channel.writeAndFlush(msg).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        ctx.channel().read();
                        LOGGER.info(channel.remoteAddress().toString());
                    }
                }
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (s2oChannel != null) {
            closeOnFlush(s2oChannel);
        }
        if (s3oChannel != null) {
            closeOnFlush(s3oChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        closeOnFlush(ctx.channel());
    }

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    private void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}