package com.netease.cloud.nsf.configuration;

import com.netease.cloud.nsf.web.resolver.RequestBodyAndHeaderResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/29
 **/
@Configuration
public class WebPluginConfig extends WebMvcConfigurerAdapter {
    @Autowired
    private RequestBodyAndHeaderResolver requestBodyAndHeaderResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(requestBodyAndHeaderResolver);
    }
}
