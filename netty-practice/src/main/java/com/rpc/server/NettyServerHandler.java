package com.rpc.server;

import com.rpc.common.RpcRequest;
import com.rpc.common.RpcResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    // 统计服务器接收到的RPC请求的总次数
    private static final AtomicInteger atomicInter = new AtomicInteger(1);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            // 转换接收的请求
            RpcRequest rpcRequest = (RpcRequest) msg;
            logger.info("server receive msg: [{}], times:[{}]", rpcRequest, atomicInter.getAndIncrement());
            RpcResponse messageFromServer = RpcResponse.builder().message("message from server").build();
            ChannelFuture f = ctx.writeAndFlush(messageFromServer);
            f.addListener(ChannelFutureListener.CLOSE);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("server catch exception", cause);
        ctx.close();
    }
}
