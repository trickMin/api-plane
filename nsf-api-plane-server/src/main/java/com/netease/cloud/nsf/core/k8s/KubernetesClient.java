package com.netease.cloud.nsf.core.k8s;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.http.DefaultK8sHttpClient;
import com.netease.cloud.nsf.util.K8sResourceEnum;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.utils.URLUtils;
import okhttp3.*;
import org.springframework.util.StringUtils;

import java.util.List;


/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/30
 **/
public class KubernetesClient extends DefaultK8sHttpClient {

    public KubernetesClient(Config config, OkHttpClient httpClient, EditorContext editorContext) {
        super(config, httpClient, editorContext);
    }


    public String get(String kind, String namespace, String name) {
        String url = getUrl(kind, namespace, name);
        return getWithNull(url);
    }

    public <T> T getObject(String kind, String namespace, String name) {
        String url = getUrl(kind, namespace, name);
        String obj = getWithNull(url);
        if (StringUtils.isEmpty(obj)) return null;
        K8sResourceGenerator gen = K8sResourceGenerator.newInstance(obj, ResourceType.JSON, editorContext);
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(gen.getKind());
        return (T) ResourceGenerator.newInstance(obj, ResourceType.JSON, editorContext).object(resourceEnum.mappingType());
    }

    public <T> List<T> getObjectList(String kind, String namespace) {
        String url = getUrl(kind, namespace);
        String obj = getWithNull(url);
        if (StringUtils.isEmpty(obj)) return null;
        K8sResourceGenerator gen = K8sResourceGenerator.newInstance(obj, ResourceType.JSON, editorContext);
        K8sResourceEnum resourceEnum = K8sResourceEnum.getItem(gen.getKind());
        return ResourceGenerator.newInstance(obj, ResourceType.JSON, editorContext).object(resourceEnum.mappingListType()).getItems();
    }

    public <T> T getObject(String url) {
        String obj = getWithNull(url);
        if (StringUtils.isEmpty(obj)) return null;

        K8sResourceGenerator generator = K8sResourceGenerator.newInstance(obj, ResourceType.JSON, editorContext);
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(generator.getKind());
        return (T) generator.object(resourceEnum.mappingType());
    }

    public <T> List<T> getObjectList(String url) {
        String obj = getWithNull(url);
        if (StringUtils.isEmpty(obj)) return null;

        K8sResourceGenerator generator = K8sResourceGenerator.newInstance(obj, ResourceType.JSON, editorContext);
        K8sResourceEnum resourceEnum = K8sResourceEnum.getItem(generator.getKind());
        return generator.object(resourceEnum.mappingListType()).getItems();
    }

    public void createOrUpdate(Object obj, ResourceType resourceType) {
        K8sResourceGenerator gen = K8sResourceGenerator.newInstance(obj, resourceType, editorContext);
        String url = getUrl(gen.getKind(), gen.getNamespace(), gen.getName());

        String oldResource = get(url);

        if (oldResource != null) {
            K8sResourceGenerator oldGenerator = K8sResourceGenerator.newInstance(oldResource, ResourceType.JSON, editorContext);
            gen.setResourceVersion(resourceVersionGenerator(oldGenerator.getResourceVersion()));
            put(url, gen.jsonString());
            return;
        }
        post(url, gen.jsonString());
    }

    public void delete(String kind, String namespace, String name) {
        String url = getUrl(kind, namespace, name);
        delete(url);
    }


    public String getUrl(String kind, String namespace) {
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(kind);
        return resourceEnum.selfLink(config.getMasterUrl(), namespace);
    }

    public String getUrl(String kind, String namespace, String name) {
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(kind);
        return URLUtils.pathJoin(resourceEnum.selfLink(config.getMasterUrl(), namespace), name);
    }

    private String resourceVersionGenerator(String oldResourceVersion) {
        return oldResourceVersion;
    }

}
