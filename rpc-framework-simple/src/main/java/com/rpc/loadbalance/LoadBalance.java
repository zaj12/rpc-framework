package com.rpc.loadbalance;

import com.rpc.extension.SPI;
import com.rpc.remoting.dto.RpcRequest;

import java.util.List;

@SPI
public interface LoadBalance {
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}
