package com.netease.cloud.nsf.core.k8s;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.http.DefaultK8sHttpClient;
import com.netease.cloud.nsf.core.k8s.http.K8sHttpClient;
import io.fabric8.kubernetes.client.Config;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/30
 **/
@Component
public class KubernetesClient {

    private K8sHttpClient baseClient;

    private EditorContext editorContext;


    @Autowired
    public KubernetesClient(Config config, OkHttpClient httpClient, EditorContext editorContext) {
        baseClient = new DefaultK8sHttpClient(config, httpClient, editorContext);
        this.editorContext = editorContext;
    }

    /**
     * 资源不存在时返回null
     */
    public <T> T getObject(String kind, String namespace, String name, Class<T> outType) {
        String obj = baseClient.getWithNull(kind, namespace, name);
        if (StringUtils.isEmpty(obj)) return null;
        return ResourceGenerator.newInstance(obj, ResourceType.JSON, editorContext).object(outType);
    }

    /**
     * 资源不存在时返回null
     */
    public String get(String kind, String namespace, String name) {
        return baseClient.getWithNull(kind, namespace, name);
    }

    public void createOrUpdate(Object obj, ResourceType resourceType) {
        K8sResourceGenerator generator = K8sResourceGenerator.newInstance(obj, resourceType, editorContext);
        String oldResource = get(generator.getKind(), generator.getNamespace(), generator.getName());
        if (oldResource != null) {
            K8sResourceGenerator oldGenerator = K8sResourceGenerator.newInstance(oldResource, ResourceType.JSON, editorContext);
            generator.setResourceVersion(resourceVersionGenerator(oldGenerator.getResourceVersion()));
            baseClient.put(generator.jsonString());
            return;
        }
        baseClient.post(generator.jsonString());
    }

    public void delete(String kind, String namespace, String name) {
        baseClient.delete(kind, namespace, name);
    }


    private String resourceVersionGenerator(String oldResourceVersion) {
        return oldResourceVersion;
    }
}
