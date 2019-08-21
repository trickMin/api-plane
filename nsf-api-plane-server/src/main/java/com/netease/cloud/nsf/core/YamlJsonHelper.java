package com.netease.cloud.nsf.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/8/19
 **/
@Component
public class YamlJsonHelper {

    private static final Logger logger = LoggerFactory.getLogger(YamlJsonHelper.class);

    @Autowired
    @Qualifier("yaml")
    private ObjectMapper yamlMapper;

    @Autowired
    private ObjectMapper jsonMapper;

    public String json2Yaml(String json) {

        if (StringUtils.isEmpty(json)) return json;
        try {
            return yamlMapper.writeValueAsString(jsonMapper.readTree(json));
        } catch (IOException e) {
            logger.warn("json to yaml failed", e);
            throw new ApiPlaneException("json to yaml failed" + e.getMessage());
        }
    }

    public String yaml2Json(String yaml) {

        if (StringUtils.isEmpty(yaml)) return yaml;
        try {
            return jsonMapper.writeValueAsString(yamlMapper.readValue(yaml, Object.class));
        } catch (IOException e) {
            logger.warn("yaml to json failed", e);
            throw new ApiPlaneException("yaml to json failed" + e.getMessage());
        }
    }

}
