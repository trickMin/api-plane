package org.hango.cloud.util.errorcode;


public class ApiPlaneErrorCode {

    public static ErrorCode Success = new ErrorCode(ErrorCodeEnum.SUCCESS);

    public static ErrorCode InvalidBodyFormat = new ErrorCode(ErrorCodeEnum.INVALID_BODY_FORMAT);

    public static ErrorCode InternalServerError = new ErrorCode(ErrorCodeEnum.INTERNAL_SERVER_ERROR);

    public static ErrorCode resourceNotFound = resourceNotFoundErrorCode();
    public static ErrorCode workLoadNotFound = workLoadNotFoundErrorCode();
    public static ErrorCode sidecarInjectPolicyError = genSidecarInjectPolicyError();
    public static ErrorCode workLoadNotInMesh = workLoadNotInMesh();

    public static ErrorCode InvalidFormat(String param) {
        return new ErrorCode(ErrorCodeEnum.INVALID_FORMAT, param);
    }

    public static ErrorCode ParameterError(String param) {
        return new ErrorCode(ErrorCodeEnum.PARAMETER_ERROR, param);
    }

    public static ErrorCode CanNotFound(String param) {
        return new ErrorCode(ErrorCodeEnum.CAN_NOT_FOUND, param);
    }

    public static ErrorCode MissingParamsError(String paramName) {
        return new ErrorCode(ErrorCodeEnum.MISSING_PARAMETER, paramName);
    }

    public static ErrorCode genSidecarInjectPolicyError(){
        ErrorCode errorCode = new ErrorCode();
        errorCode.setStatusCode(400);
        errorCode.setCode("PolicyError");
        errorCode.setMessage("当前资源所属名称空间已禁用自动注入");
        errorCode.setEnMessage("Injection for pod in namespace is disabled");
        return errorCode;
    }

    private static ErrorCode resourceNotFoundErrorCode() {
        ErrorCode errorCode = new ErrorCode(ErrorCodeEnum.RESOURCE_NOT_FOUND);
        errorCode.setCode("404");
        errorCode.setMessage("目标配置不存在");
        errorCode.setEnMessage("The target config does not exist");
        return errorCode;
    }

    private static ErrorCode workLoadNotFoundErrorCode() {
        ErrorCode errorCode = new ErrorCode(ErrorCodeEnum.RESOURCE_NOT_FOUND);
        errorCode.setCode("404");
        errorCode.setMessage("工作负载不存在");
        errorCode.setEnMessage("The workload does not exist");
        return errorCode;
    }

    private static ErrorCode workLoadNotInMesh() {
        ErrorCode errorCode = new ErrorCode(ErrorCodeEnum.QUERY_PARAMETER_ERROR, null);
        errorCode.setCode("400");
        errorCode.setMessage("该负载未加入网格");
        errorCode.setEnMessage("The workload is not added to the mesh");
        return errorCode;
    }

    /**
     * 负载均衡相关
     */
    public static ErrorCode InvalidSlowStartWindow = new ErrorCode(ErrorCodeEnum.INVALID_SLOW_START_WINDOW);
    public static ErrorCode InvalidLoadBanlanceType = new ErrorCode(ErrorCodeEnum.INVALID_LOAD_BANLANCE_TYPE);
    public static ErrorCode InvalidSimpleLoadBanlanceType = new ErrorCode(ErrorCodeEnum.INVALID_SIMPLE_LOAD_BANLANCE_TYPE);
    public static ErrorCode InvalidConsistentHashObject = new ErrorCode(ErrorCodeEnum.INVALID_CONSISTENT_HASH_OBJECT);
    public static ErrorCode InvalidConsistentHashType = new ErrorCode(ErrorCodeEnum.INVALID_CONSISTENT_HASH_TYPE);
    public static ErrorCode InvalidConsistentHashHttpCookieObject = new ErrorCode(ErrorCodeEnum.INVALID_CONSISTENT_HASH_HTTP_COOKIE_OBJECT);
    public static ErrorCode InvalidConsistentHashHttpCookieName = new ErrorCode(ErrorCodeEnum.INVALID_CONSISTENT_HASH_HTTP_COOKIE_NAME);
    public static ErrorCode InvalidConsistentHashHttpCookieTtl = new ErrorCode(ErrorCodeEnum.INVALID_CONSISTENT_HASH_HTTP_COOKIE_TTL);
    public static ErrorCode InvalidHttp1MaxPendingRequests = new ErrorCode(ErrorCodeEnum.INVALID_HTTP1_MAX_PENDING_REQUESTS);
    public static ErrorCode InvalidHttp2MaxRequests = new ErrorCode(ErrorCodeEnum.INVALID_HTTP2_MAX_REQUESTS);
    public static ErrorCode InvalidIdleTimeout = new ErrorCode(ErrorCodeEnum.INVALID_IDLE_TIMEOUT);
    public static ErrorCode InvalidMaxRequestsPerConnection = new ErrorCode(ErrorCodeEnum.INVALID_MAX_REQUESTS_PER_CONNECTION);
    public static ErrorCode InvalidMaxConnections = new ErrorCode(ErrorCodeEnum.INVALID_MAX_CONNECTIONS);
    public static ErrorCode InvalidConnectTimeout = new ErrorCode(ErrorCodeEnum.INVALID_CONNECT_TIMEOUT);

}
