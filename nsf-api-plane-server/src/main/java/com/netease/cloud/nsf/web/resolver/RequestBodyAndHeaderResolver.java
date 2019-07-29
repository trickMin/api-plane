package com.netease.cloud.nsf.web.resolver;

import com.google.common.collect.Maps;
import com.netease.cloud.nsf.web.annotation.RequestBodyAndHeader;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/29
 **/
@Component
public class RequestBodyAndHeaderResolver extends RequestResponseBodyMethodProcessor {
    public RequestBodyAndHeaderResolver(List<HttpMessageConverter<?>> converters) {
        super(converters);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestBodyAndHeader.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Object result = super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
        HashMap<String, String> headerParams = Maps.newHashMapWithExpectedSize(8);
        Iterator<String> headerNames = webRequest.getHeaderNames();
        headerNames.forEachRemaining(headerName -> headerParams.put(headerName, webRequest.getHeader(headerName)));
        BeanUtils.populate(result, headerParams);
        return result;
    }
}
