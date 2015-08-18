package com.capgemini.iot;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * HTTP Server Initializer.
 * 
 * @author Ludovic Toinel <ludovic.toinel@capgemini.com>
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

	public HttpServerInitializer() {
	}

	@Override
	public void initChannel(SocketChannel ch) {
		ChannelPipeline p = ch.pipeline();

		p.addLast(new HttpServerCodec());
		p.addLast(new HttpObjectAggregator(128));
		p.addLast(new HttpServerHandler());
	}
}