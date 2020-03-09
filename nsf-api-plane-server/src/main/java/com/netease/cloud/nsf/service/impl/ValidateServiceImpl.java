package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.k8s.validator.ConstraintViolation;
import com.netease.cloud.nsf.core.k8s.validator.IntegratedValidator;
import com.netease.cloud.nsf.meta.ValidateResult;
import com.netease.cloud.nsf.meta.ViolationItem;
import com.netease.cloud.nsf.service.ValidateService;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
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
    public ValidateResult validate(List<HasMetadata> resources) {

        Set<ConstraintViolation<HasMetadata>> violations = new LinkedHashSet<>();
        for (HasMetadata resource : resources) {
            violations.addAll(validator.validate(resource));
        }
        return getValidateResult(violations);
    }

    private ValidateResult getValidateResult(Set<ConstraintViolation<HasMetadata>> violations) {
        ValidateResult result = new ValidateResult();
        if (CollectionUtils.isEmpty(violations)) {
            result.setPass(true);
        }
        for (ConstraintViolation<HasMetadata> violation : violations) {
            ViolationItem item = new ViolationItem();
            item.setKind(Optional.ofNullable(violation.getBean()).map(HasMetadata::getKind).orElse("Unknown"));
            item.setNamespace(Optional.ofNullable(violation.getBean()).map(HasMetadata::getMetadata).map(ObjectMeta::getNamespace).orElse("Unknown"));
            item.setName(Optional.ofNullable(violation.getBean()).map(HasMetadata::getMetadata).map(ObjectMeta::getName).orElse("Unknown"));
            item.setMessage(Optional.ofNullable(violation.getMessage()).orElse("Unknown"));
            item.setValidator(Optional.ofNullable(violation.getValidator()).map(Object::getClass).map(Class::getSimpleName).orElse("Unknown"));
            result.getItems().add(item);
        }
        return result;
    }
}
