package com.rpc.common;

import com.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

/**
 * 自定义编码器。
 * 网络传输需要通过字节流来实现，ByteBuf 可以看作是 Netty 提供的字节数据的容器，使用它会
 * 让我们更加方便地处理字节数据。
 */

@AllArgsConstructor
public class NettyKryoEncoder extends MessageToByteEncoder {
    private final Serializer serializer;
    private final Class<?> genericClass;

    /**
     * 将对象转换为字节码，并写入到 ByteBuf 对象中
     * @param channelHandlerContext
     * @param o
     * @param byteBuf
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (genericClass.isInstance(o)) {
            // 1. 将对象转换为byte
            byte[] body = serializer.serialize(o);
            // 2. 读取消息长度
            int dataLength = body.length;
            // 3. 写入消息对应的字节数组长度，writerIndex 加4
            byteBuf.writeInt(dataLength);
            // 4. 将字节数组写入 byteBuf 对象中
            byteBuf.writeBytes(body);
        }
    }
}
