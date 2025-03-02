package com.rpc.remoting.transport.netty.client;

import com.rpc.enums.CompressTypeEnum;
import com.rpc.enums.SerializationTypeEnum;
import com.rpc.enums.ServiceDiscoveryEnum;
import com.rpc.extension.ExtensionLoader;
import com.rpc.factory.SingletonFactory;
import com.rpc.registry.ServiceDiscovery;
import com.rpc.remoting.constants.RpcConstants;
import com.rpc.remoting.dto.RpcMessage;
import com.rpc.remoting.dto.RpcRequest;
import com.rpc.remoting.dto.RpcResponse;
import com.rpc.remoting.transport.RpcRequestTransport;
import com.rpc.remoting.transport.netty.codec.RpcMessageDecoder;
import com.rpc.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyRpcClient implements RpcRequestTransport {
    private final ServiceDiscovery serviceDiscovery;
    private final UnprocessedRequests unprocessedRequests;
    private final ChannelProvider channelProvider;
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    public NettyRpcClient() {
        // 初始化资源: EventLoopGroup, Bootstrap......
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                // 设置超时时间: 如果超时或连接没有建立，则连接失败
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        // 如果15s内没有数据发送给服务端，则会发送心跳请求
                        p.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        p.addLast(new RpcMessageEncoder());
                        p.addLast(new RpcMessageDecoder());
                        p.addLast(new NettyRpcClientHandler());
                    }
                });
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class)
                .getExtension(ServiceDiscoveryEnum.ZK.getName());
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }

    /**
     * 连接服务器并获取channel，从而能通过channel发送消息给服务端
     * @param inetSocketAddress 服务地址
     * @return channel
     */
    @SneakyThrows  // 用于自动处理受检异常（checked exceptions）
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        // CompletableFuture 用于异步管理 Channel 的连接结果
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        // 通过 bootstrap（通常是一个 Netty 的 Bootstrap 实例）异步连接到指定的 inetSocketAddress
        // 当连接完成时触发回调
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("The client has connected [{}] successful!", inetSocketAddress.toString());
                completableFuture.complete(future.channel());  // 将连接成功的 Channel 对象设置到 CompletableFuture 中
            } else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();  // 阻塞当前线程，等待 CompletableFuture 完成，并返回其结果
    }


    /**
     * 传输 rpc 请求( RpcRequest ) 到服务端
     * 服务端的响应结果会被设置到 resultFuture 中。
     * 客户端可以通过 resultFuture.get() 或 resultFuture.whenComplete() 获取响应结果。
     * @param rpcRequest
     * @return
     */
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // 声明返回值
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        // 获取服务地址
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        // 获取服务地址关联的通道
        Channel channel = getChannel(inetSocketAddress);
        if (channel.isActive()) {
            // 将请求放进未处理请求队列中
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            RpcMessage rpcMessage = RpcMessage.builder().data(rpcRequest)
                    .codec(SerializationTypeEnum.HESSION.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE).build();
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    // 请求成功后，在ClientHandler中会把响应结果保存到resultFuture中
                    log.info("client send message: [{}]", rpcMessage);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed: ", future.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }

        return resultFuture;
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    public void close() {
        eventLoopGroup.shutdownGracefully();
    }
}
