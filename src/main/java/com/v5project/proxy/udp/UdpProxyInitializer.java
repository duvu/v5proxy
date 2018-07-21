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

import com.v5project.proxy.ProxiesConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpProxyInitializer extends ChannelInitializer<NioDatagramChannel> {

    private final String remoteHost;
    private final int remotePort;

    private final String remoteHost2;
    private final int remotePort2;

    Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public UdpProxyInitializer(ProxiesConfig.Proxy proxy) {
        this.remoteHost = proxy.getRemoteList().get(0).getHost();
        this.remotePort = proxy.getRemoteList().get(0).getPort();
        this.remoteHost2 = proxy.getRemoteList().get(1).getHost();
        this.remotePort2 = proxy.getRemoteList().get(1).getPort();
    }

    @Override
    protected void initChannel(NioDatagramChannel ch) throws Exception {
        LOGGER.info("initChannel ...");
        ch.pipeline()
                .addLast(new LoggingHandler(LogLevel.INFO))
                .addLast(new UdpFrontendHandler(remoteHost, remotePort,remoteHost2,remotePort2));
    }
}
