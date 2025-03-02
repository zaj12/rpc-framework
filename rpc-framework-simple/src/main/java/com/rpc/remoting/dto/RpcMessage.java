package com.rpc.remoting.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class RpcMessage {
    private byte messageType;
    // 序列化类型
    private byte codec;
    // 压缩类型
    private byte compress;
    private int requestId;
    private Object data;
}
