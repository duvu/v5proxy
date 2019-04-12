package com.v5project.proxy;

import com.v5project.proxy.config.ConfigurationManager;
import com.v5project.proxy.config.ProxyEntry;
import com.v5project.proxy.tcp.TcpBackendHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		List<ProxyEntry> list = ConfigurationManager.getProxyList();
		log.info("Starting ProxyEntry: " + list.size());
		try {
			ProxyManager.getInstance().init();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ExecutorService threadPool = Executors.newFixedThreadPool(4);
		threadPool.submit(new TcpClientRunnable());
		threadPool.submit(new TcpClientRunnable());
		threadPool.submit(new TcpClientRunnable());
		threadPool.submit(new TcpClientRunnable());
	}

	private static final class TcpClientRunnable implements Runnable {
		NioEventLoopGroup eventLoop = new NioEventLoopGroup(4);
		@Override
		public void run() {
			while (true) {
				TrakObject tko = QueueManager.getInstance().take();
				if (tko != null) {
					log.info(String.format("Sending to: %s:%d", tko.getHost(), tko.getPort()));
					Bootstrap tcpClient = new Bootstrap();
					tcpClient.group(eventLoop)
							.channel(NioSocketChannel.class)
							.handler(new DiscardServerHandler())
							.option(ChannelOption.AUTO_READ, true);

					ChannelFuture server2Future = tcpClient.connect(tko.getHost(), tko.getPort());
					Channel channel = server2Future.channel();
					server2Future.addListener(ChannelFutureListener.CLOSE);
					Object msg = tko.getData();
					channel.writeAndFlush("Test");
				} else {
					try {
						Thread.sleep(60 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
