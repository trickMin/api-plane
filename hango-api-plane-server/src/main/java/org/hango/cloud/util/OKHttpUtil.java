package org.hango.cloud.util;

import okhttp3.*;
import org.apache.commons.collections.MapUtils;
import org.hango.cloud.meta.dto.ApiPlaneResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/26 15:07
 **/
@Component
public class OKHttpUtil {
    private static final Logger logger = LoggerFactory.getLogger(OKHttpUtil.class);

    private static OkHttpClient okHttpClient;

    @Autowired
    public OKHttpUtil(OkHttpClient okHttpClient) {
        OKHttpUtil.okHttpClient = okHttpClient;
    }

    /**
     * GET Method begin---------------------------------
     */

    public static ApiPlaneResult<String> get(@Nonnull String url, Map<String, String> headerParameter, Map<String, String> queryParameter){
        Request request = getRequestBuilder(url, headerParameter, queryParameter).build();
        return process(request);
    }

    /**
     * POST Method With JSON begin---------------------------------
     */
    public static ApiPlaneResult<String> post(@Nonnull String url, Map<String, String> headerParameter, Map<String, String> queryParameter, String body){
        Request request = processPostJsonParameter(url, headerParameter, queryParameter, body);
        return process(request);
    }

    private static ApiPlaneResult<String> process(Request request){
        try (Response resp = okHttpClient.newCall(request).execute()) {
            if (!resp.isSuccessful()){
                return ApiPlaneResult.ofHttpRemoteError(resp.message());
            }
            if (resp.body() == null){
                return ApiPlaneResult.ofHttpRemoteError("响应体为空");
            }
            return ApiPlaneResult.ofSuccess(resp.body().string());
        }catch (Exception e){
            logger.error("远程Http请求异常, url:{}", request.url(), e);
            return ApiPlaneResult.ofHttpRemoteError("远程Http调用异常");
        }
    }



    private static Request processPostJsonParameter(String url, Map<String, String> headerParameter, Map<String, String> queryParameter, String bodyStr) {
        Request.Builder builder = getRequestBuilder(url, headerParameter, queryParameter);
        if (StringUtils.isEmpty(bodyStr)){
            bodyStr = "{}";
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyStr);
        builder.post(body);
        return builder.build();
    }

    private static Request.Builder getRequestBuilder(String url, Map<String, String> headerParameter, Map<String, String> queryParameter){
        Request.Builder builder = new Request.Builder();
        if (MapUtils.isNotEmpty(headerParameter)) {
            builder.headers(Headers.of(headerParameter));
        }
        if (MapUtils.isEmpty(queryParameter)) {
            builder.url(url);
        } else {
            boolean hasQuery;
            try {
                hasQuery = !StringUtils.isEmpty(new URL(url).getQuery());
            } catch (MalformedURLException e) {
                throw new RuntimeException("url is illegal");
            }
            StringBuilder sb = new StringBuilder(url);
            if (!hasQuery) {
                sb.append("?1=1");
            }
            queryParameter.forEach((k, v) -> {
                sb.append("&").append(k).append("=").append(v);
            });
            builder.url(sb.toString());
        }
        return builder;
    }

}
