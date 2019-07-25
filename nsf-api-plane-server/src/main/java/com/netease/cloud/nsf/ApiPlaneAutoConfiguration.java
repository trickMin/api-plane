package com.netease.cloud.nsf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/18
 **/
@Configuration
public class ApiPlaneAutoConfiguration {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}