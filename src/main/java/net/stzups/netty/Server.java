package net.stzups.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.IOException;

public class Server implements AutoCloseable {
    private final int port;

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    public Server(int port) {
        this.port = port;

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
    }

    public ChannelFuture start(ChannelHandler handler) throws Exception {
        return start(handler, new LoggingHandler("netty", LogLevel.DEBUG));
    }

    /**
     * Initializes the server and binds to the specified port
     * @return close future
     */
    public ChannelFuture start(ChannelHandler handler, LoggingHandler loggingHandler) throws Exception {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(loggingHandler)
                .childHandler(handler);

        ChannelFuture bindFuture = serverBootstrap.bind(port).await();
        if (!bindFuture.isSuccess()) {
            throw new IOException("Failed to bind to port " + port, bindFuture.cause());
        }

        return bindFuture.channel().closeFuture();
    }

    @Override
    public void close() {
        workerGroup.shutdownGracefully().syncUninterruptibly();
        bossGroup.shutdownGracefully().syncUninterruptibly();
    }
}