package com.netease.cloud.nsf.core.k8s.validator;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/3/5
 **/
public class ConstraintViolation {
    private String message;
    private Object bean;
    private Object validator;


    public ConstraintViolation(String message, Object bean, Object validator) {
        this.message = message;
        this.bean = bean;
        this.validator = validator;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public Object getValidator() {
        return validator;
    }

    public void setValidator(Object validator) {
        this.validator = validator;
    }
}
