package com.rpc.config;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceConfig {
    // 服务版本
    private String version = "";

    // 当接口有多个实现类，用group来区分
    private String group = "";

    // 目标服务
    private Object service;

    public String getRpcServiceName() {
        return this.getRpcServiceName() + this.getGroup() + this.getVersion();
    }

    public String getServiceName() {
        // 获取服务实现的第一个接口的完全限定名
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
