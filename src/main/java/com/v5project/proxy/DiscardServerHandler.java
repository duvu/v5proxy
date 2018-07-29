package com.v5project.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import static com.v5project.proxy.ProxyManager.PROXY_CHANNEL_TYPE;

/**
 * Handles a server-side channel.
 */
public class DiscardServerHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // discard
        ProxyManager.ChannelType channelType = ctx.channel().attr(PROXY_CHANNEL_TYPE).get();
        if (ProxyManager.ChannelType.CHANNEL_TCP == channelType) {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ProxyManager.ChannelType channelType = ctx.channel().attr(PROXY_CHANNEL_TYPE).get();
        if (ProxyManager.ChannelType.CHANNEL_TCP == channelType) {
            ctx.close();
        }
    }
}