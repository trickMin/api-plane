package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.template.TemplateTranslator;
import com.netease.cloud.nsf.core.template.TemplateUtils;
import com.netease.cloud.nsf.core.template.TemplateWrapper;
import com.netease.cloud.nsf.meta.PluginTemplate;
import com.netease.cloud.nsf.service.PluginService;
import com.sun.javafx.binding.StringFormatter;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.netease.cloud.nsf.util.PathExpressionEnum.*;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/2
 **/
@Service
public class PluginServiceImpl implements PluginService {
    private static final String LABEL_DESCRIPTION = "description";
    private static final String LABEL_TYPE = "type";
    private static final String LABEL_VERSION = "version";

    @Autowired
    private Configuration configuration;

    @Autowired
    private EditorContext editorContext;

    @Autowired
    private TemplateTranslator templateTranslator;


    @Override
    public PluginTemplate getTemplate(String name, String version) {
        // 1. get TemplateWrapper
        Template template = TemplateUtils.getTemplate(getTemplateName(name), configuration);
        TemplateWrapper wrapper = TemplateUtils.getWrapperWithFilter(template,
                templateWrapper -> templateWrapper.containLabel(LABEL_TYPE, "pluginTemplate") && templateWrapper.containLabel(LABEL_VERSION, version)
        );

        // 2. create PluginTemplate
        PluginTemplate pluginTemplate = new PluginTemplate();
        pluginTemplate.setName(name);
        pluginTemplate.setVersion(version);
        pluginTemplate.setDescription(wrapper.getLabelValue(LABEL_DESCRIPTION));
        pluginTemplate.setSchema(ResourceGenerator.newInstance(wrapper.getSource(), ResourceType.JSON, editorContext).object(Map.class));

        return pluginTemplate;
    }

    @Override
    public String processSchema(String plugin) {
        // 1. get TemplateInfo
        ResourceGenerator gen = ResourceGenerator.newInstance(plugin, ResourceType.JSON, editorContext);
        String kind = gen.getValue(PLUGIN_GET_KIND.translate());
        String version = gen.getValue(PLUGIN_GET_VERSION.translate());

        // 2. get TemplateWrapper
        Template template = TemplateUtils.getTemplate(getTemplateName(kind), configuration);
        TemplateWrapper wrapper = TemplateUtils.getWrapperWithFilter(template,
                templateWrapper -> templateWrapper.containLabel(LABEL_TYPE, "istioSchema") && templateWrapper.containLabel(LABEL_VERSION, version)
        );

        // 3. process scheme
        return processWithJson(wrapper.get(), plugin);
    }


    private String processWithJson(Template template, String json) {
        ResourceGenerator modelGen = ResourceGenerator.newInstance(json, ResourceType.JSON, editorContext);
        return templateTranslator.translate(template, modelGen.object(Map.class));
    }

    private String getTemplateName(String name) {
        return StringFormatter.format("plugin/%s.ftl", name).getValue();
    }
}
