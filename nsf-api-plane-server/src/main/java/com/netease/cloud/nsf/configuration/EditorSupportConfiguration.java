package com.netease.cloud.nsf.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.netease.cloud.nsf.core.editor.EditorContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import com.jayway.jsonpath.Configuration;

import java.util.EnumSet;
import java.util.Set;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
@org.springframework.context.annotation.Configuration
public class EditorSupportConfiguration {
    @Bean
    public Configuration configuration(ObjectMapper objectMapper) {
        Configuration.setDefaults(new Configuration.Defaults() {
            private final JsonProvider jsonProvider = new JacksonJsonProvider(objectMapper);
            private final MappingProvider mappingProvider = new JacksonMappingProvider(objectMapper);

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });
        return Configuration.builder().options(Option.DEFAULT_PATH_LEAF_TO_NULL).build();
    }

    @Bean
    public EditorContext editorContext(ObjectMapper jsonMapper, @Qualifier("yaml") ObjectMapper yamlMapper, Configuration configuration) {
        return new EditorContext(jsonMapper, yamlMapper, configuration);
    }
}
