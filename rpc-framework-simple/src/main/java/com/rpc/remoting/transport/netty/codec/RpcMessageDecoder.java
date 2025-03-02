package com.rpc.remoting.transport.netty.codec;

import com.rpc.compress.Compress;
import com.rpc.enums.CompressTypeEnum;
import com.rpc.enums.SerializationTypeEnum;
import com.rpc.extension.ExtensionLoader;
import com.rpc.remoting.constants.RpcConstants;
import com.rpc.remoting.dto.RpcMessage;
import com.rpc.remoting.dto.RpcRequest;
import com.rpc.remoting.dto.RpcResponse;
import com.rpc.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;


/**
 *   0     1     2     3     4        5    6    7    8      9           10      11       12    13   14   15   16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+--------+-----+----+----+----+
 *   |   magic   code        |version | full length         |messageType| codec |compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 */

@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {
    public RpcMessageDecoder() {
        // lengthFiledOffset = magic code(4B) + version(1B)
        // lengthFieldLength = 4B
        // lengthAdjustment，因为长度字段的值（fullLength）包含了整个消息的长度，而在这之前已经读取了 9 个字节，所以实际需要读取的字节数是 fullLength - 9
        // initialBytesToStrip，因为不需要跳过字节，因此为0
        this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    /**
     * @param maxFrameLength 决定可接收数据的最大长度，如果超出长度则会丢弃数据
     * @param lengthFiledOffset 长度字段的偏移量
     * @param lengthFiledLength 长度字段的字节数
     * @param lengthAdjustment  对长度字段的值进行补偿调整。实际读取的字节数 = 长度字段的值 + lengthAdjustment。
     *                          用于处理长度字段的值是否包含自身长度或其他部分。
     * @param initialBytesToStrip 从解码后的消息中去掉前几个字节。
     */
    public RpcMessageDecoder(int maxFrameLength, int lengthFiledOffset, int lengthFiledLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFiledOffset, lengthFiledLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error", e);
                } finally {
                    frame.release();
                }
            }
        }
        return decoded;
    }

    private Object decodeFrame(ByteBuf in) {
        // 有序读取ByteBuf
        checkMagicNumber(in);
        checkVersion(in);
        int fullLength = in.readInt();
        // 构建RpcMessage对象
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();
        RpcMessage rpcMessage = RpcMessage.builder()
                .codec(codecType)
                .requestId(requestId)
                .messageType(messageType).build();

        if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }

        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if (bodyLength > 0) {
            byte[] bs = new byte[bodyLength];
            in.readBytes(bs);
            // 解压数据
            String compressName = CompressTypeEnum.getName(compressType);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressName);
            bs = compress.decompress(bs);
            // 反序列化
            String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
            log.info("codec name: [{}] ", codecName);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codecName);
            if (messageType == RpcConstants.REQUEST_TYPE) {
                RpcRequest tmpValue = serializer.deserialize(bs, RpcRequest.class);
                rpcMessage.setData(tmpValue);
            } else {
                RpcResponse tmpValue = serializer.deserialize(bs, RpcResponse.class);
                rpcMessage.setData(tmpValue);
            }
        }
        return rpcMessage;
    }

    private void checkVersion(ByteBuf in) {
        // 读取版本并比较
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
    }

    private void checkMagicNumber(ByteBuf in) {
        // 读取魔数并比较
        int length = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[length];
        in.readBytes(tmp);
        for (int i = 0; i < length; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unkown magic code: " + Arrays.toString(tmp));
            }
        }
    }
}
