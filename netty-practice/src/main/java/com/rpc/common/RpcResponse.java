package com.rpc.common;

import lombok.*;

/**
 * 服务端响应实体类
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Getter
public class RpcResponse {
    private String message;
}
