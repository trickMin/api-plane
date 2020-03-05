package com.netease.cloud.nsf.util.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/3/5
 **/
public class K8sResourceValidateException extends ApiPlaneException {
    private List<Object> violation = new ArrayList<>();

    public K8sResourceValidateException() {
        super("istio resource validate failed.");
    }

    public List<Object> getViolation() {
        return violation;
    }

    public void setViolation(List<Object> violation) {
        this.violation = violation;
    }
}
