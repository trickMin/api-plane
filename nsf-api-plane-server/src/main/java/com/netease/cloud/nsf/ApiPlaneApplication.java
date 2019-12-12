package com.netease.cloud.nsf;

import com.netease.cloud.nsf.configuration.YamlPropertyLoaderFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/15
 **/

@SpringBootApplication
@PropertySource(value = "classpath:k8s.yaml", factory = YamlPropertyLoaderFactory.class)
@EnableConfigurationProperties
@EnableScheduling
public class ApiPlaneApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ApiPlaneApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ApiPlaneApplication.class);
    }
}