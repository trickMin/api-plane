package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.k8s.validator.ConstraintViolation;
import com.netease.cloud.nsf.core.k8s.validator.IntegratedValidator;
import com.netease.cloud.nsf.service.ValidateService;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.K8sResourceValidateException;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/3/5
 **/
@Service
public class ValidateServiceImpl implements ValidateService {

    @Autowired
    private IntegratedValidator validator;

    @Override
    public void validate(List<HasMetadata> resources) throws K8sResourceValidateException {

        Set<ConstraintViolation<HasMetadata>> violations = new LinkedHashSet<>();
        for (HasMetadata resource : resources) {
            violations.addAll(validator.validate(resource));
        }
        if (!CollectionUtils.isEmpty(violations)) {
            throw getException(violations);
        }
    }

    private K8sResourceValidateException getException(Set<ConstraintViolation<HasMetadata>> violations) {
        List<Object> items = new ArrayList<>();
        for (ConstraintViolation<HasMetadata> violation : violations) {
            ResourceGenerator item = ResourceGenerator.newInstance(String.format("{\"kind\":\"%s\",\"name\":\"%s\",\"namespace\":\"%s\",\"validator\":\"%s\",\"message\":\"%s\"}",
                    violation.getBean().getKind(),
                    violation.getBean().getMetadata().getName(),
                    violation.getBean().getMetadata().getNamespace(),
                    violation.getValidator().getClass().getSimpleName(),
                    StringEscapeUtils.escapeJava(violation.getMessage())
            ));
            items.add(item.object(Object.class));
        }
        K8sResourceValidateException validateException = new K8sResourceValidateException();
        validateException.setViolation(items);
        return validateException;
    }
}
