package com.rpc.remoting.transport;

import com.rpc.extension.SPI;
import com.rpc.remoting.dto.RpcRequest;

/**
 * 发送rpc请求和接收请求的接口
 */
@SPI
public interface RpcRequestTransport {
    Object sendRpcRequest(RpcRequest rpcRequest);
}
