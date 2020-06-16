package com.netease.cloud.nsf.configuration;

import net.devh.springboot.autoconfigure.grpc.server.GrpcServerLifecycle;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/6/15
 **/
@AutoConfigureBefore(ApiPlaneAutoBaseConfiguration.class)
public class GatewayAutoConfiguration {

    @PostConstruct
    public void init() {
        System.out.println("gateway auto config");
    }

    // 不自动启用grpc server
    @Bean
    @Profile(value = "!sm")
    public GrpcServerLifecycle grpcServerLifecycle() {
        return null;
    }
}
