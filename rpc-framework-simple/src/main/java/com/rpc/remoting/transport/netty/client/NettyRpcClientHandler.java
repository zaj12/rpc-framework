package com.rpc.remoting.transport.netty.client;


import com.rpc.enums.CompressTypeEnum;
import com.rpc.enums.SerializationTypeEnum;
import com.rpc.factory.SingletonFactory;
import com.rpc.remoting.constants.RpcConstants;
import com.rpc.remoting.dto.RpcMessage;
import com.rpc.remoting.dto.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;


/**
 * 处理服务端返回的请求
 *  如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@linkSimpleChannelInboundHandler} 内部的
 *  channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    /**
     * 将服务端返回的 RPC 响应结果（rpcResponse）与客户端之前发出的请求关联起来，
     * 并将结果设置到对应的 CompletableFuture 中
     * @param ctx
     * @param msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            log.info("client receive msg: [{}]", msg);
            RpcMessage tmp = (RpcMessage) msg;
            byte messageType = tmp.getMessageType();
            if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                // 这是一个心跳响应，记录日志即可
                log.info("heart [{}]", tmp.getData());
            } else if (messageType == RpcConstants.RESPONSE_TYPE) {
                RpcResponse<Object> rpcResponse = (RpcResponse<Object>) tmp.getData();
                unprocessedRequests.complete(rpcResponse);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 用于处理用户自定义事件（如空闲事件）
     * 当检测到写空闲时，客户端会发送一个心跳消息（PING）到服务端，以维持连接。如果发送失败，则关闭 Channel。
     * 维持长连接通信场景。
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 判断触发的事件是否是 IdleStateEvent（空闲状态事件）
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            // 判断当前的空闲状态是否是 WRITER_IDLE（写空闲）, 表示通道有一段时间没有写操作
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                // 创建心跳消息
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                rpcMessage.setData(RpcConstants.PING);
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            // 如果触发的事件不是 IdleStateEvent，则调用父类的 userEventTriggered 方法处理
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 当处理客户端消息异常时调用
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("client catch exception: ", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
