package ru.gb;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

public class Server {

    public static void main(String[] args) {
        System.out.println("[ SERVER STARTED ]");
        try {

            Path storage = Paths.get("server-storage");
            if (!Files.exists(storage)) {
                Files.createDirectory(storage);
            }

            DBConnector.connect();

            new Server();

        } catch (ClassNotFoundException | IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    public Server() {
        EventLoopGroup mainGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(mainGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(
                                    new ObjectDecoder(256*1024*1024, ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new AuthHandler(),
                                    new AbstractHandler(),
                                    new ChunkedWriteHandler()
                            );
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(8188).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mainGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            DBConnector.disconnect();
        }
    }
}