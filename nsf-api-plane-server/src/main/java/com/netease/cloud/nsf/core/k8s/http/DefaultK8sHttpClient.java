package com.netease.cloud.nsf.core.k8s.http;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.ResourceConflictException;
import com.sun.javafx.binding.StringFormatter;
import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.api.model.StatusBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.utils.URLUtils;
import okhttp3.*;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/31
 **/
public class DefaultK8sHttpClient implements K8sHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(DefaultK8sHttpClient.class);

    protected static final MediaType JSON = MediaType.parse("application/json");
    protected static final String CLIENT_STATUS_FLAG = "CLIENT_STATUS_FLAG";

    protected Config config;
    protected OkHttpClient httpClient;
    protected EditorContext editorContext;


    public DefaultK8sHttpClient(Config config, OkHttpClient httpClient, EditorContext editorContext) {
        this.config = config;
        this.httpClient = httpClient;
        this.editorContext = editorContext;
    }


    protected void assertResponseCode(Request request, Response response) {
        int statusCode = response.code();
        String customMessage = config.getErrorMessages().get(statusCode);

        if (response.isSuccessful()) {
            return;
        } else if (customMessage != null) {
            throw requestFailure(request, createStatus(statusCode, customMessage));
        } else {
            throw requestFailure(request, createStatus(response));
        }
    }

    protected String handleResponse(Request.Builder requestBuilder) {
        return handleResponse(requestBuilder, (request, response) -> assertResponseCode(request, response), null);
    }

    protected String handleResponse(Request.Builder requestBuilder, BiConsumer<Request, Response> responseConsumer, Function<Response, String> responseMapper) {
        Request request = requestBuilder.build();
        if (Objects.nonNull(request)) {
            logger.info(request.toString());
        }
        Buffer buffer = new Buffer();
        try {
            if (Objects.nonNull(request.body()) && request.body().contentLength() != 0) {
                request.body().writeTo(buffer);
                logger.info("Request body: \n{}", ResourceGenerator.prettyJson(buffer.readString(Charset.defaultCharset())));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (Response response = httpClient.newCall(request).execute()) {
            if (Objects.nonNull(responseConsumer)) responseConsumer.accept(request, response);
            logger.info(response.toString());
            if (Objects.nonNull(responseMapper)) return responseMapper.apply(response);
            if (Objects.nonNull(response.body())) {
                String responseBody = response.body().string();
                logger.info("Response body: \n{}", ResourceGenerator.prettyJson(responseBody));
                return responseBody;
            }
            return null;
        } catch (IOException e) {
            throw new ApiPlaneException(StringFormatter.format("K8s request failed : {}.", request.toString()).toString(), e);
        }
    }


    protected Status createStatus(String statusMessage) {
        ResourceGenerator generator = ResourceGenerator.newInstance(statusMessage, ResourceType.JSON, editorContext);
        return generator.object(Status.class);
    }

    protected Status createStatus(Response response) {
        String statusMessage = "";
        ResponseBody body = response != null ? response.body() : null;
        int statusCode = response != null ? response.code() : 0;
        try {
            if (response == null) {
                statusMessage = "No response";
            } else if (body != null) {
                statusMessage = body.string();
            } else if (response.message() != null) {
                statusMessage = response.message();
            }
            Status status = createStatus(statusMessage);
            if (status.getCode() == null) {
                status = new StatusBuilder(status).withCode(statusCode).build();
            }
            return status;
        } catch (IOException e) {
            return createStatus(statusCode, statusMessage);
        }
    }

    protected Status createStatus(int statusCode, String message) {
        Status status = new StatusBuilder()
                .withCode(statusCode)
                .withMessage(message)
                .build();
        status.getAdditionalProperties().put(CLIENT_STATUS_FLAG, "true");
        return status;
    }

    protected ApiPlaneException requestFailure(Request request, Status status) {
        StringBuilder sb = new StringBuilder();
        sb.append("Failure executing: ").append(request.method())
                .append(" at: ").append(request.url().toString()).append(".");

        if (status.getMessage() != null && !status.getMessage().isEmpty()) {
            sb.append(" Message: ").append(status.getMessage()).append(".");
        }

        if (!status.getAdditionalProperties().containsKey(CLIENT_STATUS_FLAG)) {
            sb.append(" Received status: ").append(status).append(".");
        }

        return new ApiPlaneException(sb.toString());
    }

    public String getMasterUrl() {
        return config.getMasterUrl();
    }

    public String getUrl(String kind, String namespace) {
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(kind);
        return resourceEnum.selfLink(config.getMasterUrl(), namespace);
    }

    public String getUrl(String kind, String namespace, String name) {
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(kind);
        return URLUtils.pathJoin(resourceEnum.selfLink(config.getMasterUrl(), namespace), name);
    }

    public String getUrlWithLabels(String url, Map<String, String> labels) {
        if (!CollectionUtils.isEmpty(labels)) {
            StringBuilder sb = new StringBuilder("labelSelector=");
            List<String> kvs = new ArrayList<>();
            labels.forEach((k, v) -> kvs.add(k + "%3D" + v));
            sb.append(String.join(",", kvs));
            return url + "?" + sb.toString();
        }
        return url;
    }

    public String getUrlWithLabels(String kind, String namespace, Map<String, String> labels) {
        String url = getUrl(kind, namespace);
        return getUrlWithLabels(url, labels);
    }

    public String getUrlWithLabels(String kind, String namespace, String name, Map<String, String> labels) {
        String url = getUrl(kind, name, namespace);
        return getUrlWithLabels(url, labels);
    }

    @Override
    public String getWithNull(String url) {
        Request.Builder requestBuilder = new Request.Builder().get().url(url);
        return handleResponse(requestBuilder, null, resp -> {
            try {
                if (!resp.isSuccessful()) return null;
                if (Objects.nonNull(resp.body())) {
                    String responseBody = resp.body().string();
                    logger.info("Response body: \n{}", ResourceGenerator.prettyJson(responseBody));
                    return responseBody;
                }
                return null;
            } catch (IOException e) {
                throw new ApiPlaneException(e.getMessage(), e);
            }
        });
    }

    @Override
    public String get(String url) {
        Request.Builder requestBuilder = new Request.Builder().get().url(url);
        return handleResponse(requestBuilder);
    }

    @Override
    public String put(String url, String resource) {
        RequestBody body = RequestBody.create(JSON, resource);
        Request.Builder requestBuilder = new Request.Builder().put(body).url(url);
        return handleResponse(requestBuilder, (request, response) -> {
            int statusCode = response.code();
            if (statusCode == HttpStatus.CONFLICT.value()) {
                throw new ResourceConflictException(String.format("Resource put %s conflict.", url));
            }
            assertResponseCode(request, response);
        }, null);
    }

    @Override
    public String post(String url, String resource) {
        RequestBody body = RequestBody.create(JSON, resource);
        Request.Builder requestBuilder = new Request.Builder().post(body).url(url);
        return handleResponse(requestBuilder);
    }

    @Override
    public String delete(String url) {
        Request.Builder requestBuilder = new Request.Builder().delete().url(url);
        return handleResponse(requestBuilder);
    }
}
