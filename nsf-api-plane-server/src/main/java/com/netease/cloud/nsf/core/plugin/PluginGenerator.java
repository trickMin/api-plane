package com.netease.cloud.nsf.core.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.jayway.jsonpath.Configuration;
import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/3/13
 **/
public class PluginGenerator extends ResourceGenerator {

    protected static EditorContext defaultContext = new EditorContext(new ObjectMapper(), new YAMLMapper(), Configuration.defaultConfiguration());

    public static void configDefaultContext(EditorContext editorContext) {
        defaultContext = editorContext;
    }

    protected PluginGenerator(Object resource, ResourceType type, EditorContext editorContext) {
        super(resource, type, editorContext);
    }

    public static PluginGenerator newInstance(Object resource, ResourceType type, EditorContext editorContext) {
        return new PluginGenerator(resource, type, editorContext);
    }

    public static PluginGenerator newInstance(Object resource, ResourceType type) {
        return new PluginGenerator(resource, type, defaultContext);
    }

    public static PluginGenerator newInstance(Object resource) {
        return new PluginGenerator(resource, ResourceType.JSON, defaultContext);
    }
}
