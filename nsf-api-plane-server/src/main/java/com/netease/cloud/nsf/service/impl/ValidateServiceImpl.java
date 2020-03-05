package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.k8s.validator.ConstraintViolation;
import com.netease.cloud.nsf.core.k8s.validator.IntegratedValidator;
import com.netease.cloud.nsf.service.ValidateService;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/3/5
 **/
@Service
public class ValidateServiceImpl implements ValidateService {

    @Autowired
    private IntegratedValidator validator;

    @Override
    public void validate(List<HasMetadata> resources) throws ApiPlaneException {

        Set<ConstraintViolation> violations = new LinkedHashSet<>();
        for (HasMetadata resource : resources) {
            violations.addAll(validator.validate(resource));
        }
        if (!CollectionUtils.isEmpty(violations)) {
            throw getException(violations);
        }
    }

    private ApiPlaneException getException(Set<ConstraintViolation> violations) {
        ResourceGenerator generator = ResourceGenerator.newInstance("{\"failed\":\"Istio resource validate failed.\",\"items\":[]}");
        for (ConstraintViolation violation : violations) {
            String item = String.format("{\"obj\":\"%s\",\"validator\":\"%s\",\"message\":\"%s\"}",
                    Optional.ofNullable(violation.getBean()).orElse("Unknown"),
                    Optional.ofNullable(violation.getValidator()).orElse("Unknown"),
                    Optional.ofNullable(StringEscapeUtils.escapeJava(violation.getMessage())).orElse("Unknown")
            );
            generator.addJsonElement("$.items", item);
        }
        return new ApiPlaneException(generator.jsonString());
    }
}
