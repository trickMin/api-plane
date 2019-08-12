package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.plugin.SchemaProcessor;
import com.netease.cloud.nsf.core.template.TemplateTranslator;
import com.netease.cloud.nsf.core.template.TemplateUtils;
import com.netease.cloud.nsf.core.template.TemplateWrapper;
import com.netease.cloud.nsf.meta.PluginTemplate;
import com.netease.cloud.nsf.meta.ServiceInfo;
import com.netease.cloud.nsf.service.PluginService;
import com.sun.javafx.binding.StringFormatter;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.netease.cloud.nsf.util.PathExpressionEnum.*;
import static com.netease.cloud.nsf.util.PluginConst.*;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/2
 **/
@Service
public class PluginServiceImpl implements PluginService {
    @Autowired
    private Configuration configuration;

    @Autowired
    private EditorContext editorContext;

    @Autowired
    private TemplateTranslator templateTranslator;

    @Autowired
    private ApplicationContext applicationContext;


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
    public String processSchema(String plugin, ServiceInfo serviceInfo) {
        // 1. get TemplateInfo
        ResourceGenerator gen = ResourceGenerator.newInstance(plugin, ResourceType.JSON, editorContext);
        String kind = gen.getValue(PLUGIN_GET_KIND.translate());
        String version = gen.getValue(PLUGIN_GET_VERSION.translate());

        // 2. get TemplateWrapper
        Template template = TemplateUtils.getTemplate(getTemplateName(kind), configuration);
        TemplateWrapper wrapper = TemplateUtils.getWrapperWithFilter(template,
                templateWrapper -> templateWrapper.containLabel(LABEL_TYPE, "istioSchema") && templateWrapper.containLabel(LABEL_VERSION, version)
        );

        // 3. process with processor or json
        if (wrapper.containKey(LABEL_PROCESSOR)) {
            String processBean = wrapper.getLabelValue(LABEL_PROCESSOR);
            Map<String, SchemaProcessor> processors = applicationContext.getBeansOfType(SchemaProcessor.class);
            List<Map.Entry<String, SchemaProcessor>> processorList = processors.entrySet().stream().filter(p -> p.getValue().getName().equals(processBean)).collect(Collectors.toList());
            if (!processorList.isEmpty()) {
                return processorList.get(0).getValue().process(plugin, serviceInfo);
            }
        }
        return processWithJsonAndSvc(wrapper.get(), plugin, serviceInfo);
    }

    //todo: target可能不是标准的svc形式
    @Override
    public List<String> extractService(List<String> plugins) {
        List<String> ret = new ArrayList<>();
        plugins.stream().forEach(plugin -> {
            ResourceGenerator rg = ResourceGenerator.newInstance(plugin, ResourceType.JSON, editorContext);
            ret.addAll(rg.getValue("$.rule[*].action.target"));
            ret.addAll(rg.getValue("$.rule[*].action.pass_proxy_target[*].url"));
        });
        return ret.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
    }

    private String processWithJsonAndSvc(Template template, String json, Object svcInstance) {
        ResourceGenerator jsonGen = ResourceGenerator.newInstance(json, ResourceType.JSON, editorContext);
        ResourceGenerator instanceGen = ResourceGenerator.newInstance(svcInstance, ResourceType.OBJECT, editorContext);
        jsonGen.createOrUpdateValue("$", "svc", instanceGen.object(Map.class));
        String obj = jsonGen.jsonString();
        return templateTranslator.translate(template, jsonGen.object(Map.class));
    }

    private String processWithJson(Template template, String json) {
        ResourceGenerator modelGen = ResourceGenerator.newInstance(json, ResourceType.JSON, editorContext);
        return templateTranslator.translate(template, modelGen.object(Map.class));
    }

    private String getTemplateName(String name) {
        return StringFormatter.format("plugin/%s.ftl", name).getValue();
    }
}
