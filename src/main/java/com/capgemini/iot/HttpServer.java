package com.capgemini.iot;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * IoT HTTP Server Request Handler.
 * 
 * @author Ludovic Toinel <ludovic.toinel@capgemini.com>
 */
public final class HttpServer {

	// Http Port
    static final int PORT = 8080;

    public static void main(String[] args) throws Exception {

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             //.handler(new LoggingHandler(LogLevel.ERROR))
             .childHandler(new HttpServerInitializer());

            Channel ch = b.bind(PORT).sync().channel();

            System.out.println("Edge Node started on => http://127.0.0.1:" + PORT + '/');

            ch.closeFuture().sync();
            
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}