package org.hango.cloud.util.errorcode;

/**
 *
 */
public class ExceptionHandlerErrorCode extends ErrorCode {

    protected ExceptionHandlerErrorCode(ErrorCodeEnum errorCodeEnum, String[] args) {
		super(errorCodeEnum, args);
	}

	public static ErrorCode InternalServerError = new ErrorCode(ErrorCodeEnum.INTERNAL_SERVER_ERROR);

	public static ErrorCode UnknownException = new ErrorCode(ErrorCodeEnum.UNKNOWN_EXCEPTION);

	public static ErrorCode InvalidBodyFormat = new ErrorCode(ErrorCodeEnum.INVALID_BODY_FORMAT);

	public static ErrorCode ResourceConflict = new ErrorCode(ErrorCodeEnum.RESOURCE_CONFLICT);

	public static ErrorCode InvalidParamsError(String paramName,String paramValue){
		return new ErrorCode(ErrorCodeEnum.INVALID_PARAMETER_VALUE, paramName, paramValue);
	}

	public static ErrorCode InvalidParameterValue(Object value, String name) {
		return new ErrorCode(ErrorCodeEnum.INVALID_PARAMETER_VALUE, name, String.valueOf(value));
	}

	public static ErrorCode BadRequest(String param) {
		return new ErrorCode(ErrorCodeEnum.CUSTOM_BAD_REQUEST,param);
	}

	public static ErrorCode MissingParamsType(String param) {
		return new ErrorCode(ErrorCodeEnum.MISSING_PARAMETER, param);
	}

	public static ErrorCode CustomInvalidBodyFormat(String msg) {
		return new ErrorCode(ErrorCodeEnum.INVALID_BODY_FORMAT_CUSTOM, msg);
	}

}
