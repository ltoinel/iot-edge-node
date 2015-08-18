package com.capgemini.iot;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.bson.Document;

import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;

/**
 * HTTP Server Request Handler.
 * 
 * @author Ludovic Toinel <ludovic.toinel@capgemini.com>
 */
public class HttpServerHandler extends ChannelHandlerAdapter {

	private static MongoCollection<Document> collection;

	private static final String UTF8 = "UTF-8";
			
	/**
	 * Default constructor
	 */
	public HttpServerHandler() {
		super();

		// Retrieve the Mongo Collection
		if (collection == null){
			MongoClient mongoClient = MongoClients.create("mongodb://localhost");
			MongoDatabase database = mongoClient.getDatabase("iot");
			collection = database.getCollection("message");
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws UnsupportedEncodingException {
		
		// Full HTTP request
		if (msg instanceof FullHttpRequest) {
			
			FullHttpRequest request = (FullHttpRequest) msg;
			String content = null;

			// Retrieve the content
			if (request.content().isReadable()) {
				ByteBuffer nioBuffer = request.content().nioBuffer();
				byte[] bytes = new byte[nioBuffer.remaining()];
				nioBuffer.get(bytes);
				content = new String(bytes, UTF8);
			}

			// Create a new document
			Document doc = new Document("timestamp", System.currentTimeMillis())
					.append("content", content);

			// Save the Data into the mongoDb
			collection.insertOne(doc, new SingleResultCallback<Void>() {
			    public void onResult(final Void result, final Throwable t) {
			       // System.out.println("Document inserted!");
			    }
			});
			
			// Check the keep alive
			FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
					HttpResponseStatus.OK, Unpooled.wrappedBuffer("Success".getBytes()));

			// Set the response header
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
			response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH,
					response.content().readableBytes());

			// Write the response
			ChannelFuture future = ctx.write(response);
			future.addListener(ChannelFutureListener.CLOSE); 
		}
	}

	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}