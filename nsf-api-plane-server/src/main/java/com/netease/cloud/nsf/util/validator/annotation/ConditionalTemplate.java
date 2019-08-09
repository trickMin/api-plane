package com.netease.cloud.nsf.util.validator.annotation;

import com.netease.cloud.nsf.util.validator.ConditionalTemplateValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Repeatable(ConditionalTemplates.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ConditionalTemplateValidator.class})
public @interface ConditionalTemplate {

    String message() default "This field is required.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String templateName();
    String[] required();
}