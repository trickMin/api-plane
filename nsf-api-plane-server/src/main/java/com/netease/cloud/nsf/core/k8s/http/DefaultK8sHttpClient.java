package com.netease.cloud.nsf.core.k8s.http;

import com.fasterxml.jackson.core.JsonParseException;
import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.K8sResourceGenerator;
import com.netease.cloud.nsf.meta.K8sResourceEnum;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.sun.javafx.binding.StringFormatter;
import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.api.model.StatusBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.utils.URLUtils;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/31
 **/
public class DefaultK8sHttpClient implements K8sHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(DefaultK8sHttpClient.class);

    protected static final MediaType JSON = MediaType.parse("application/json");
    protected static final MediaType JSON_PATCH = MediaType.parse("application/json-patch+json");
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

    protected String handleResponse(Request.Builder requestBuilder, boolean strictMode) {
        Request request = requestBuilder.build();
        logger.info("K8s resource " + request.toString());
        try (Response response = httpClient.newCall(request).execute()) {
            if (strictMode) {
                assertResponseCode(request, response);
            }
            String result = response.body().string();
            logger.debug(result);
            return result;
        } catch (IOException e) {
            throw new ApiPlaneException(StringFormatter.format("K8s request failed : {}.", request.toString()).toString(), e);
        }
    }

    protected String handleResponse(Request.Builder requestBuilder) {
        return handleResponse(requestBuilder, true);
    }


    protected Status createStatus(String statusMessage) {
        ResourceGenerator generator = ResourceGenerator.newInstance(statusMessage, ResourceType.JSON, editorContext);
        Status status = generator.object(Status.class);
        return status;
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
        } catch (JsonParseException e) {
            return createStatus(statusCode, statusMessage);
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

        if (status != null && !status.getAdditionalProperties().containsKey(CLIENT_STATUS_FLAG)) {
            sb.append(" Received status: ").append(status).append(".");
        }

        return new ApiPlaneException(sb.toString());
    }

    @Override
    public String getWithNull(String kind, String namespace, String name) {
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(kind);
        String url = URLUtils.pathJoin(resourceEnum.selfLink(config.getMasterUrl(), namespace), name);

        Request.Builder requestBuilder = new Request.Builder().get().url(url);
        Request request = requestBuilder.build();
        logger.info("K8s resource " + request.toString());
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String result = response.body().string();
                logger.debug(result);
                return result;
            }
            return null;
        } catch (IOException e) {
            throw new ApiPlaneException(StringFormatter.format("K8s request failed : {}.", request.toString()).toString(), e);
        }
    }

    @Override
    public String get(String kind, String namespace, String name) {
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(kind);
        String url = URLUtils.pathJoin(resourceEnum.selfLink(config.getMasterUrl(), namespace), name);

        Request.Builder requestBuilder = new Request.Builder().get().url(url);
        return handleResponse(requestBuilder);
    }

    @Override
    public String put(String resource) {
        K8sResourceGenerator generator = K8sResourceGenerator.newInstance(resource, ResourceType.JSON, editorContext);
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(generator.getKind());
        RequestBody body = RequestBody.create(JSON, generator.jsonString());
        String url = URLUtils.pathJoin(resourceEnum.selfLink(config.getMasterUrl(), generator.getNamespace()), generator.getName());

        Request.Builder requestBuilder = new Request.Builder().put(body).url(url);
        return handleResponse(requestBuilder);
    }

    @Override
    public String post(String resource) {
        K8sResourceGenerator generator = K8sResourceGenerator.newInstance(resource, ResourceType.JSON, editorContext);
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(generator.getKind());
        RequestBody body = RequestBody.create(JSON, generator.jsonString());
        String url = resourceEnum.selfLink(config.getMasterUrl(), generator.getNamespace());

        Request.Builder requestBuilder = new Request.Builder().post(body).url(url);
        return handleResponse(requestBuilder);
    }

    @Override
    public String delete(String kind, String namespace, String name) {
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(kind);
        String url = URLUtils.pathJoin(resourceEnum.selfLink(config.getMasterUrl(), namespace), name);

        Request.Builder requestBuilder = new Request.Builder().delete().url(url);
        return handleResponse(requestBuilder);
    }
}
