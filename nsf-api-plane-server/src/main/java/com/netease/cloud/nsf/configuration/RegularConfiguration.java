package com.netease.cloud.nsf.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/23
 **/
@Configuration
public class RegularConfiguration {


    @Bean
    @Qualifier("yaml")
    ObjectMapper yamlObjectMapper() {
        return new ObjectMapper(new YAMLFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Bean
    @Primary
    ObjectMapper jsonObjectMapper() {
        return new ObjectMapper();
    }
}
