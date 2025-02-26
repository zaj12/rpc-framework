package com.rpc.common;

import com.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@AllArgsConstructor
@Slf4j
public class NettyKryoDecoder extends ByteToMessageDecoder {
    private final Serializer serializer;
    private final Class<?> genericClass;
    /**
     * Netty传输的消息长度（对象序列化后对应的字节数组大小），存储在bytebuf头部
     */
    private static final int BODY_LENGTH = 4;

    /**
     * @param ctx 解码器关联的 ChannelHandlerContext 对象
     * @param in "入站"数据，也就是 ByteBuf 对象
     * @param out 解码之后的数据对象需要添加到 out 对象里面
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 1. byteBuf中写入的消息长度所占字节数已经为4，因此byteBuf可读字节必须大于4
        if (in.readableBytes() >= BODY_LENGTH) {
            // 2. 标记当前readIndex的位置，以便后面重置readIndex
            in.markReaderIndex();
            // 3. 读取消息长度
            // 注意： 消息长度是encode的时候我们自己写入的，参见 NettyKryoEncoder 的encode方法
            int dataLength = in.readInt();
            // 4. 不合理情况直接return
            if (dataLength < 0 || in.readableBytes() < 0) {
                log.error("data length or byteBuf readableBytes is not valid");
                return;
            }
            // 5. 如果可读字节数小于消息长度，说明是不完整的信息，重置readIndex
            if (in.readableBytes() < dataLength) {
                in.resetReaderIndex();
                return;
            }
            // 6. 消息没有问题，进行序列化
            byte[] body = new byte[dataLength];
            in.readBytes(body);
            // 将bytes数组转换为对象
            Object obj = serializer.deserialize(body, genericClass);
            out.add(obj);
            log.info("successful decode ByteBuf to Object");
        }
    }
}
