package com.netease.cloud.nsf;

import com.google.common.collect.ImmutableList;
import com.netease.cloud.nsf.util.rest.RestTemplateLogInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/15
 **/

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
public class ApiPlaneApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ApiPlaneApplication.class, args);
    }


    @Bean
    RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        List<ClientHttpRequestInterceptor> interceptors = ImmutableList.of(new RestTemplateLogInterceptor());

        return restTemplateBuilder
                .interceptors(interceptors)
                .requestFactory(new InterceptingClientHttpRequestFactory(
                        new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()), interceptors))
                .build();
    }
}