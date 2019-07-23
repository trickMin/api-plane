package com.netease.cloud.nsf.util.errorcode;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/22
 **/
public class ApiPlaneErrorCode {

    public static ErrorCode Success = new ErrorCode(ErrorCodeEnum.Success);

    public static ErrorCode InvalidBodyFormat = new ErrorCode(ErrorCodeEnum.InvalidBodyFormat);

    public static ErrorCode resourceNotFound = resourceNotFoundErrorCode();

    public static ErrorCode InvalidFormat(String param) {
        return new ErrorCode(ErrorCodeEnum.InvalidFormat, param);
    }

    public static ErrorCode MissingParamsError(String paramName) {
        return new ErrorCode(ErrorCodeEnum.MissingParameter, paramName);
    }

    private static ErrorCode resourceNotFoundErrorCode() {
        ErrorCode errorCode = new ErrorCode(ErrorCodeEnum.ResourceNotFound);
        errorCode.setCode("404");
        errorCode.setMessage("目标配置不存在");
        errorCode.setEnMessage("The target config does not exist");
        return errorCode;
    }
}
