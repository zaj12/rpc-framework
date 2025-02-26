package com.rpc.common;

import lombok.*;

/**
 * 客户端请求实体类
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Getter
public class RpcRequest {
    private String interfaceName;
    private String methodName;
}
