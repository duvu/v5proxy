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

import com.v5project.proxy.config.ProxyEntry;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class TcpProxyInitializer extends ChannelInitializer<SocketChannel> {

    private final String remoteHost;
    private final int remotePort;
    private final int remotePort2;

    public TcpProxyInitializer(ProxyEntry proxyEntry) {
                this.remoteHost = proxyEntry.getRemoteList().get(0).getHost();
                this.remotePort = proxyEntry.getRemoteList().get(0).getPort();
//                this.remoteHost2 = proxyEntry.getRemoteList().get(1).getHost();
//                this.remotePort2 = proxyEntry.getRemoteList().get(1).getPort();
        remotePort2 = proxyEntry.getPort1();
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ch.pipeline()
                .addLast(new TcpFrontendHandler(remoteHost, remotePort,remotePort2));
    }
}
