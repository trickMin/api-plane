package com.netease.cloud.nsf.core.plugin;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/9/26
 **/
public class Plugin extends ResourceGenerator {
    protected Plugin(Object resource, ResourceType type, EditorContext editorContext) {
        super(resource, type, editorContext);
    }

    public Plugin(String json) {
        this(json, ResourceType.JSON, defaultContext);
    }

    public String getVersion() {
        return getValue("$.version");
    }

    public String getKind() {
        return getValue("$.kind");
    }
}
