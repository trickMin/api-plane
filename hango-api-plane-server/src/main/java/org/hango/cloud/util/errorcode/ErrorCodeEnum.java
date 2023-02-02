package org.hango.cloud.util.errorcode;

/**
 * 新版 OpenAPI 后使用的 ErrorCode 枚举
 * <p>
 * 该枚举为基础枚举，不能直接用于返回，需要使用 ErrorCode 进行封装
 *
 *
 * <p>
 * After 2017.09.20
 */
public enum ErrorCodeEnum {
    SUCCESS("Success", "Success", "处理成功", 200),

    INVALID_FORMAT("InvalidFormat", "The format of the input parameter %s is illegal.", "参数 %s 的格式非法", 400),
    INVALID_BODY_FORMAT("InvalidFormat", "The format of the request body is illegal.", "请求体格式非法", 400),
    INVALID_BODY_FORMAT_CUSTOM("InvalidFormat", "The format of the request body is illegal: %s", "请求体格式非法: %s", 400),
    REQUEST_EXPIRED("RequestExpired", "Request has expired.", "请求已过期", 400),
    MISSING_PARAMETER("MissingParameter", "The required input parameter %s for processing this request is not supplied.", "参数  %s 缺失", 400),
    ILLEGAL_ACCESS_KEY("IllegalAccessKey", "The access key you provided is illegal.", "AccessKey 无效", 400),
    ACCESS_KEY_NOT_FOUND("AccessKeyNotFound", "We can not found the access key you provided.", "AccessKey 不存在", 401),
    INTERNAL_SERVER_ERROR("InternalServerError", "Internal server error .", "服务器内部错误 ", 500),
    SERVER_OPTION_ERROR("ServerOptionError", "Server setting %s error ", "配置项 %s 无法设置", 500),
    INVALID_PARAMETER_VALUE("InvalidParameterValue", "The parameter %s cannot accept value %s.", "参数 %s 的值 %s 非法", 400),
    SIGNATURE_DOES_NOT_MATCH("SignatureDoesNotMatch", "The request signature we calculated does not match the signature you provided.", "签名不匹配", 403),
    INVALID_AUTHORIZATION_INFO("InvalidAuthorizationInfo", "The authorization info you provided is invalid.", "认证信息无效", 400),
    OUT_OF_BOUNDS("OutOfBounds", "%s is out of bounds", "%s超过限制", 400),
    INVALID_PARAMETER_LENGTH("InvalidparameterLength", "The parameter %s length is invalid", "参数%s长度错误", 400),
    PARAMETERS_NOT_MATCH("ParametersNotMatch", "The parameter %s cannot match parameter %s", "参数%s与参数%s不匹配", 400),
    UNKNOWN_EXCEPTION("UnknownException", "The unknown exception occurred", "出现未知错误", 400),
    CUSTOM_BAD_REQUEST("BadRequest", "%s", "%s", 400),
    QUERY_PARAMETER_ERROR("QueryParameterError", " The parameter %s is invalid", "请求参数%s无效", 400),
    PROJECT_CODE_ERROR("ProjectCodeError", "Can not get projectId with id %s", "通过标识%s查询项目Id失败", 400),
    REPEAT_VALUE("RepeatValue", "%s value is repeat", "%s值重复", 400),
    DRY_RUN_OPERATION("DryRunOperation", "The quest would have successed, but the DryRun parameter was used.", "签名认证通过，但是使用了 DryRun 参数", 400),
    API_FREQ_OUT_OF_LIMIT("ApiFreqOutOfLimit", "Api freq out of limit.", "访问频率过高，请稍后再试", 403),
    RESOURCE_CONFLICT("ResourceConflict", "Resource handling conflicts", "资源处理冲突", 409),
    // 如下几个枚举用于异常处理
    METHOD_NOT_ALLOWED("MethodNotAllow", "Http method not allowed.", "http 方法不支持", 405),
    INVALID_PARAMETERS("InvalidParameters", "Invalid parameters.", "参数无效", 400),
    UN_SUPPORTED_MEDIA_TYPE("UnSupportedMediaType", "Unsupported media type.", "不支持的媒体类型", 415),
    UN_PROCESSABLE_ENTITY("UnProcessableEntity", "Unprocessable entity.", "不可处理的实体", 422),
    UN_PROCESSABLE_PARAMETER("UnProcessableParameter", "parameter %s can't be changed.", "参数%s无法被修改", 422),
    MISSING_PARAMETERS("MissingParameters", "Missing parameters.", "参数缺失", 400),

    NO_SUCH_API("NoSuchApi", "No such api:%s.", "没有请求的API:%s", 404),


    CONFIG_ALREAD_EXIST("ConfigAlreadyExist", "The configuration you provided is already exist.", "配置已存在", 400),
    TARGET_ALREAD_EXIST("ConfigAlreadyExist", "The target service for rule is already exist.", "该目标服务已匹配治理规则", 400),
    ALREADY_EXIST("AlreadyExist", "Parameter %s you provided is already exist.", "%s 名称已存在", 400),
    STILL_EXIST("StillExist", "Parameter %s still exist", "%s 依旧存在", 400),
    CAN_NOT_DELETE("CanNotDelete", "This %s can't be deleted.", "此%s不能删除", 400),
    CAN_NOT_FOUND("ResourceNotFound", "Can't found %s", "找不到对应%s", 404),
    PARAMETER_NULL("ParameterNull", "Parameter null", "参数为空", 400),
    PARAMETER_ERROR("ParameterError", "Parameter %s error.", "参数%s错误", 400),

    NO_PERMISSION_ERROR("NoPrivate", "No private to config parameter %s", "无权设置参数%s", 401),
    DUPLICATE_NAME_ERROR("DuplicateNameError", "The value for %s is Duplicated", "%s设置重复", 400),
    DUPLICATE_METHOD_ERROR("DuplicateMethodError", "The %s is Duplicated: %s", "%s 已被选中: %s", 400),
    LOG_DIRS_AND_MOUNT_PATH_DUPLICATE_ERROR("InvalidParameterValue", "Value for %s and for %s are Duplicated", "%s 和 %s 值有重复", 400),
    CANNOT_UPDATE("CannotUpdate", "%s can't update", "%s 不能更新", 400),
    NO_PERMISSION("NoPermission", "You don't have permission to access the specified interface.", "对不起，您没有权限访问该接口。", 401),

    PARAM_NOT_MATCH("ParamNotMatch", "Param not match", "参数不匹配", 400),

    RESOURCE_NOT_FOUND("ResourceNotFound", "The resource requested is not found.", "请求的资源不存在", 404),
    USER_STATUS_ABNORMAL("UserStatusAbnormal", "User status abnormal.", "用户状态异常", 403),
    TOO_MANY_REQUEST("TooManyRequest", "Too Many Request, Please try again later.", "访问频率过快，请稍后重试。", 429),
    TIME_RANGE_TOO_LARGE("TimeRangeTooLarge", "Does not support interval queries greater than 7 days.", "暂不支持大于7天的区间查询.", 400),

    POLICY_ALREADY_EXIST("PolicyAlreadyExist", "The policy already exists, you cannot create it repeatedly.", "策略已存在，不允许重复创建", 400),

    CREATE_CREDENTIAL_FAILED("CreateCredentialFailed", "Create Credential is Failed.", "生成密钥对失败", 400),
    GET_CREDENTIAL_FAILED("GetCredentialFailed", "Get Credential is Failed.", "查询密钥对失败", 400),
    DELETE_EXTER_GROUP_FAILED("DeleteExterGroupFailed", "Delete external group is falied", "删除对外访问服务组异常", 400),
    UPDATE_EXTER_SWITCH_FAILED("UpdateExterSwitchFailed", "Update external switch is Failed.", "更新对外访问开关异常", 400),
    GET_AUTH_CLIENT_ERROR("GetAuthClientError", "Get nec-auth client is error.", "调用nce-auth接口，客户端错误", 400),
    CREATE_EXTER_GROUP_FAILED("CreateExterGroupFailed", "Create external group is Failed.", "生成对外访问服务组失败", 400),
    INVALID_VERSION_WEIGHT("InvalidVersionWeight", "The sum of weights can not be 0", "版本权重之和不能为0", 400),
    DUPLICATE_DATA_ERROR("DuplicateDataError", "Item is Duplicated in dataBase", "数据库中已存在该记录", 400),
    DATA_SYNC_ERROR("DataSyncError", "operation failed:%s", "操作失败:%s", 400),
    INSTANCE_STILL_EXIST_ERROR("StillExistError", "Instance is still exist for this service,can not cancell it", "该服务仍有实例运行，无法进行注销操作", 400),
    CONFIG_APP_EXIST_ERROR("ConfigAppExistError", "App in config server is exist", "配置中心中appId已经存在，创建服务失败", 400),
    SERVICE_NOT_FOUND("ServiceNotFound", "Can't found service with Id %s", "找不到对应Id为%s的服务", 404),
    HTTP_REMOTE_ERROR("HttpRemoteError", "http remote call faile", "http远程调用失败", 404),
    //负载均衡相关
    INVALID_SLOW_START_WINDOW("InvalidSlowStartWindow", "The service warm-up time can be configured in the range [1s-3600s] only", "服务预热时间窗仅支持配置[1s-3600s]区间", 400),
    INVALID_LOAD_BANLANCE_TYPE("InvalidLoadBanlanceType", "This load balance type is invalid", "服务负载均衡类型取值为Simple或ConsistentHash", 400),
    INVALID_SIMPLE_LOAD_BANLANCE_TYPE("InvalidSimpleLoadBanlanceType", "This simple load balance type is invalid", "Simple类型的负载均衡规则，仅包含ROUND_ROUBIN、LEAST_CONN、RANDOM", 400),
    INVALID_CONSISTENT_HASH_OBJECT("InvalidConsistentHashObject", "Consistent hash object is invalid", "一致性哈希对象格式非法", 400),
    INVALID_CONSISTENT_HASH_TYPE("InvalidConsistentHashType", "Consistent hash type is invalid", "一致性哈希对象类型为httpHeaderName、httpCookie、useSourceIp三者之一", 400),
    INVALID_CONSISTENT_HASH_HTTP_COOKIE_OBJECT("InvalidConsistentHashHttpCookieObject", "Http cookie is invalid", "一致性哈希对象使用cookie时，cookie对象不能为空", 400),
    INVALID_CONSISTENT_HASH_HTTP_COOKIE_NAME("InvalidConsistentHashHttpCookieName", "Http cookie name is invalid", "一致性哈希对象使用cookie时，cookie名称不能为空", 400),
    INVALID_CONSISTENT_HASH_HTTP_COOKIE_TTL("InvalidConsistentHashHttpCookieName", "Http cookie ttl is invalid", "一致性哈希对象使用cookie时，cookie ttl不能小于0", 400),
    INVALID_HTTP1_MAX_PENDING_REQUESTS("InvalidHttp1MaxPendingRequests", "http1MaxPendingRequests is invalid", "http1MaxPendingRequests不能小于0", 400),
    INVALID_HTTP2_MAX_REQUESTS("InvalidHttp2MaxRequests", "Http2MaxRequests is invalid", "http2MaxRequests不能小于0", 400),
    INVALID_IDLE_TIMEOUT("InvalidIdleTimeout", "IdleTimeout is invalid", "idleTimeout不能小于0", 400),
    INVALID_MAX_REQUESTS_PER_CONNECTION("InvalidMaxRequestsPerConnection", "MaxRequestsPerConnection is invalid", "maxRequestsPerConnection不能小于0", 400),
    INVALID_MAX_CONNECTIONS("InvalidmaxConnections", "MaxConnections is invalid", "maxConnections不能小于0", 400),
    INVALID_CONNECT_TIMEOUT("InvalidConnectTimeout", "ConnectTimeout is invalid", "connectTimeout不能小于0", 400);

    private String code;
    private String enMsg;
    private String msg;
    private int statusCode;

    private ErrorCodeEnum(String code, String enMsg, String msg, int statusCode) {
        this.code = code;
        this.enMsg = enMsg;
        this.msg = msg;
        this.statusCode = statusCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getEnMsg() {
        return enMsg;
    }

    public void setEnMsg(String enMsg) {
        this.enMsg = enMsg;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }


}
