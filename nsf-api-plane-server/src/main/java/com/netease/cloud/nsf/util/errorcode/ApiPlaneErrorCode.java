package com.netease.cloud.nsf.util.errorcode;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/22
 **/
public class ApiPlaneErrorCode {

    public static ErrorCode Success = new ErrorCode(ErrorCodeEnum.Success);

    public static ErrorCode InvalidBodyFormat = new ErrorCode(ErrorCodeEnum.InvalidBodyFormat);

    public static ErrorCode resourceNotFound = resourceNotFoundErrorCode();
    public static ErrorCode workLoadNotFound = workLoadNotFoundErrorCode();
    public static ErrorCode sidecarInjectPolicyError = sidecarInjectPolicyError();
    public static ErrorCode workLoadNotInMesh = workLoadNotInMesh();

    public static ErrorCode InvalidFormat(String param) {
        return new ErrorCode(ErrorCodeEnum.InvalidFormat, param);
    }

    public static ErrorCode ParameterError(String param) {
        return new ErrorCode(ErrorCodeEnum.ParameterError, param);
    }

    public static ErrorCode CanNotFound(String param) {
        return new ErrorCode(ErrorCodeEnum.CanNotFound, param);
    }

    public static ErrorCode MissingParamsError(String paramName) {
        return new ErrorCode(ErrorCodeEnum.MissingParameter, paramName);
    }

    public static ErrorCode sidecarInjectPolicyError(){
        ErrorCode errorCode = new ErrorCode();
        errorCode.setStatusCode(400);
        errorCode.setCode("PolicyError");
        errorCode.setMessage("当前资源所属名称空间已禁用自动注入");
        errorCode.setEnMessage("Injection for pod in namespace is disabled");
        return errorCode;
    }

    private static ErrorCode resourceNotFoundErrorCode() {
        ErrorCode errorCode = new ErrorCode(ErrorCodeEnum.ResourceNotFound);
        errorCode.setCode("404");
        errorCode.setMessage("目标配置不存在");
        errorCode.setEnMessage("The target config does not exist");
        return errorCode;
    }

    private static ErrorCode workLoadNotFoundErrorCode() {
        ErrorCode errorCode = new ErrorCode(ErrorCodeEnum.ResourceNotFound);
        errorCode.setCode("404");
        errorCode.setMessage("工作负载不存在");
        errorCode.setEnMessage("The workload does not exist");
        return errorCode;
    }

    private static ErrorCode workLoadNotInMesh() {
        ErrorCode errorCode = new ErrorCode(ErrorCodeEnum.QueryParameterError, null);
        errorCode.setCode("400");
        errorCode.setMessage("该负载未加入网格");
        errorCode.setEnMessage("The workload is not added to the mesh");
        return errorCode;
    }
}
