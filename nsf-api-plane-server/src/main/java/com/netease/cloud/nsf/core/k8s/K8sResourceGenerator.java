package com.netease.cloud.nsf.core.k8s;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.netease.cloud.nsf.util.PathExpressionEnum.*;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/31
 **/
public final class K8sResourceGenerator extends ResourceGenerator {


    private K8sResourceGenerator(Object resource, ResourceType type, EditorContext editorContext) {
        super(resource, type, editorContext);
    }

    public static K8sResourceGenerator newInstance(String json, EditorContext editorContext) {
        return new K8sResourceGenerator(json, ResourceType.JSON, editorContext);
    }

    public static K8sResourceGenerator newInstance(Object resource, ResourceType type, EditorContext editorContext) {
        return new K8sResourceGenerator(resource, type, editorContext);
    }

    public String getName() {
        return getValue(YANXUAN_GET_NAME.translate());
    }

    public String getNamespace() {
        return getValue(YANXUAN_GET_NAMESPACE.translate());
    }

    public String getKind() {
        return getValue(YANXUAN_GET_KIND.translate());
    }

    public String getApiVersion() {
        return getValue(YANXUAN_GET_APIVERSION.translate());
    }

    public String getResourceVersion() {
        return getValue(YANXUAN_GET_RESOURCEVERSION.translate());
    }

    public void setName(String name) {
        updateValue(YANXUAN_GET_NAME.translate(), name);
    }

    public void setNamespace(String namespace) {
        updateValue(YANXUAN_GET_NAMESPACE.translate(), namespace);
    }

    public void setKind(String kind) {
        updateValue(YANXUAN_GET_KIND.translate(), kind);
    }

    public void setApiVersion(String apiVersion) {
        updateValue(YANXUAN_GET_APIVERSION.translate(), apiVersion);
    }

    public void setResourceVersion(String resourceVersion) {
        updateValue(YANXUAN_GET_RESOURCEVERSION.translate(), resourceVersion);
    }

    public boolean isList() {
        Pattern pattern = Pattern.compile("(.*)List$");
        return pattern.matcher(getKind()).find();
    }

    public List<String> items() {
        if (!isList()) {
            throw new ApiPlaneException("Cant convert Object to List Type.");
        }
        List<String> ret = new ArrayList<>();
        List objs = getValue(YANXUAN_GET_ITEMS.translate());
        objs.forEach(obj -> ret.add(ResourceGenerator.newInstance(obj, ResourceType.OBJECT, editorContext).jsonString()));
        return ret;
    }

    public <T> List<T> items(Class<T> itemsType) {
        if (!isList()) {
            throw new ApiPlaneException("Cant convert Object to List Type.");
        }
        List<T> ret = new ArrayList<>();
        List<String> jsons = items();
        jsons.forEach(json -> ret.add(ResourceGenerator.newInstance(json, ResourceType.JSON, editorContext).object(itemsType)));
        return ret;
    }
}
