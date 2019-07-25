package com.netease.cloud.nsf.util.exception;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/6/20
 **/
public class ApiPlaneException extends RuntimeException {

    public ApiPlaneException() {}

    public ApiPlaneException(String message) {
        super(message);
    }

    public ApiPlaneException(String message, Throwable cause) {
        super(message, cause);
    }
}
