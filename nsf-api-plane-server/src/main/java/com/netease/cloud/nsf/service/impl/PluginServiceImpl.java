package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.plugin.SchemaProcessor;
import com.netease.cloud.nsf.core.template.TemplateTranslator;
import com.netease.cloud.nsf.core.template.TemplateUtils;
import com.netease.cloud.nsf.core.template.TemplateWrapper;
import com.netease.cloud.nsf.meta.PluginTemplate;
import com.netease.cloud.nsf.meta.ServiceInfo;
import com.netease.cloud.nsf.service.PluginService;
import com.netease.cloud.nsf.util.K8sResourceEnum;
import com.sun.javafx.binding.StringFormatter;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.netease.cloud.nsf.util.PathExpressionEnum.*;
import static com.netease.cloud.nsf.util.LabelConst.*;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/2
 **/
@Service
public class PluginServiceImpl implements PluginService {

    private static final Logger logger = LoggerFactory.getLogger(PluginServiceImpl.class);

    @Autowired
    private Configuration configuration;

    @Autowired
    private EditorContext editorContext;

    @Autowired
    private TemplateTranslator templateTranslator;

    @Autowired
    private List<SchemaProcessor> processors;


    @Override
    public PluginTemplate getTemplate(String name, String version) {
        // 1. get TemplateWrapper
        Template template = TemplateUtils.getTemplate(getTemplateName(name), configuration);
        TemplateWrapper wrapper = TemplateUtils.getWrapperWithFilter(template,
                templateWrapper -> templateWrapper.containLabel(LABEL_TYPE, VALUE_PLUGIN_SCHEMA) && templateWrapper.containLabel(LABEL_VERSION, version)
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
    public FragmentHolder processSchema(String plugin, ServiceInfo serviceInfo) {
        // 1. get TemplateInfo
        ResourceGenerator gen = ResourceGenerator.newInstance(plugin, ResourceType.JSON, editorContext);
        String kind = gen.getValue(PLUGIN_GET_KIND.translate());
        String version = gen.getValue(PLUGIN_GET_VERSION.translate());

        // 2. get TemplateWrapper
        Template template = TemplateUtils.getTemplate(getTemplateName(kind), configuration);
        TemplateWrapper wrapper = TemplateUtils.getWrapperWithFilter(template,
                templateWrapper -> templateWrapper.containLabel(LABEL_TYPE, VALUE_ISTIO_FRAGMENT) && templateWrapper.containLabel(LABEL_VERSION, version)
        );

        // 3. process with processor or json
        if (wrapper.containKey(LABEL_PROCESSOR)) {
            String processBean = wrapper.getLabelValue(LABEL_PROCESSOR);
            return processors.stream().filter(processor -> processBean.equals(processor.getName())).findAny().get().process(plugin, serviceInfo);
        }
        return processWithJsonAndSvc(wrapper, plugin, serviceInfo);
    }

    @Override
    public List<String> extractService(List<String> plugins) {
        List<String> ret = new ArrayList<>();
        plugins.stream().forEach(plugin -> {
            ResourceGenerator rg = ResourceGenerator.newInstance(plugin, ResourceType.JSON, editorContext);
            ret.addAll(rg.getValue("$.rule[*].action.target"));
            ret.addAll(rg.getValue("$.rule[*].action.pass_proxy_target[*].url"));
        });
        return ret.stream().distinct().filter(Objects::nonNull).map(item -> {
            try {
                URI uri = new URI(item);
                return uri.getHost();
            } catch (URISyntaxException e) {
                logger.warn("Not standard uri format : {}", item);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private FragmentHolder processWithJsonAndSvc(TemplateWrapper templateWrapper, String json, ServiceInfo svcInstance) {
        ResourceGenerator jsonGen = ResourceGenerator.newInstance(json, ResourceType.JSON, editorContext);
        if (!Objects.isNull(svcInstance)) {
            ResourceGenerator instanceGen = ResourceGenerator.newInstance(svcInstance, ResourceType.OBJECT, editorContext);
            instanceGen.object(Map.class).forEach((k, v) -> jsonGen.createOrUpdateValue("$", String.valueOf(k), v));
        }
        String content = templateTranslator.translate(templateWrapper.get(), jsonGen.object(Map.class));
        FragmentHolder holder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withResourceType(K8sResourceEnum.valueOf(templateWrapper.getLabelValue(LABEL_RESOURCE_TYPE)))
                .withFragmentType(FragmentTypeEnum.valueOf(templateWrapper.getLabelValue(LABEL_FRAGMENT_TYPE)))
                .withContent(content)
                .build();
        holder.setVirtualServiceFragment(wrapper);
        return holder;
    }

    private String getTemplateName(String name) {
        return StringFormatter.format("plugin/%s.ftl", name).getValue();
    }
}
