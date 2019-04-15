package com.v5project.proxy;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * @author beou on 4/15/19 19:26
 */
public class EventLoopFactory {
    private static EventLoopGroup worker2 = new NioEventLoopGroup(4);

    public static EventLoopGroup getWorker2() {
        return worker2;
    }
}
