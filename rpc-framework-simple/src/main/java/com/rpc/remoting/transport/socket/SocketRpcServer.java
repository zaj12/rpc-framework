package com.rpc.remoting.transport.socket;

import com.rpc.factory.SingletonFactory;
import com.rpc.config.CustomShutdownHook;
import com.rpc.config.RpcServiceConfig;
import com.rpc.provider.ServiceProvider;
import com.rpc.provider.impl.ZKServiceProviderImpl;
import com.rpc.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import static com.rpc.remoting.transport.netty.server.NettyRpcServer.PORT;

@Slf4j
public class SocketRpcServer {
    private final ExecutorService threadPool;
    private final ServiceProvider serviceProvider;

    public SocketRpcServer() {
        threadPool = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        serviceProvider = SingletonFactory.getInstance(ZKServiceProviderImpl.class);
    }

    /**
     * 服务注册
     * @param rpcSerivceConfig
     */
    public void registerService(RpcServiceConfig rpcSerivceConfig) {
        serviceProvider.publishService(rpcSerivceConfig);
    }

    public void start() {
        try (ServerSocket server = new ServerSocket()) {
            String host = InetAddress.getLocalHost().getHostAddress();
            server.bind(new InetSocketAddress(host, PORT));
            CustomShutdownHook.getCustomShutdownHook().clearAll();
            Socket socket;
            while((socket = server.accept()) != null) {
                log.info("client connected [{}]", socket.getInetAddress());
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            log.error("occur IOException", e);
        }
    }
}
