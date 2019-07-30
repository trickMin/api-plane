package com.netease.cloud.nsf.web.resolver;

import com.netease.cloud.nsf.meta.SiderCarRequestMeta;
import com.netease.cloud.nsf.web.annotation.SiderCarRequestBody;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/29
 **/
@Component
public class SiderCarRequestHeaderResolver extends RequestResponseBodyMethodProcessor {

    private static final String REQUEST_HEADER = "X-Forwarded-Client-Cert";

    public SiderCarRequestHeaderResolver(List<HttpMessageConverter<?>> converters) {
        super(converters);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(SiderCarRequestBody.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Object result = super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
        String value = webRequest.getHeader(REQUEST_HEADER);
        String[] spiltValues = value.split(";");

        // 解析by, hash, subject, uri
        SiderCarRequestMeta meta = new SiderCarRequestMeta();
        for (String spiltValue : spiltValues) {
            Pattern pattern = Pattern.compile("(.*)=(.*)");
            Matcher matcher = pattern.matcher(spiltValue);
            if (matcher.find()) {
                String k = matcher.group(1);
                String v = matcher.group(2);
                BeanUtils.copyProperty(meta, k.toLowerCase(), v);
            }
        }

        // 解析cluster, service, namespace
        String uri = meta.getUri();
        Pattern pattern = Pattern.compile("spiffe://(.*)/ns/(.*)/sa/(.*)");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            meta.setCluster(matcher.group(1));
            meta.setNamespace(matcher.group(2));
            meta.setService(matcher.group(3));
        }

        SiderCarRequestBody annotation = parameter.getParameterAnnotation(SiderCarRequestBody.class);
        BeanUtils.copyProperty(result, annotation.metaName(), meta);
        return result;
    }
}
