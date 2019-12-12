package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.PluginInstance;
import com.netease.cloud.nsf.core.plugin.processor.SchemaProcessor;
import com.netease.cloud.nsf.core.template.TemplateUtils;
import com.netease.cloud.nsf.meta.Plugin;
import com.netease.cloud.nsf.meta.ServiceInfo;
import com.netease.cloud.nsf.service.PluginService;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import freemarker.template.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.netease.cloud.nsf.core.editor.PathExpressionEnum.PLUGIN_GET_KIND;

/**
 * @auther wupenghuai@corp.netmask.com
 * @date 2019/8/2
 **/
@Service
public class PluginServiceImpl implements PluginService {

    private static final Logger logger = LoggerFactory.getLogger(PluginServiceImpl.class);

    private static final String PLUGIN_CONFIG = "plugin/plugin-config.json";

    @Autowired
    private Configuration configuration;

    @Autowired
    private EditorContext editorContext;

    @Autowired
    private List<SchemaProcessor> processors;


    @Override
    public Plugin getPlugin(String name) {
        Plugin p = getPlugins().get(name);
        if (Objects.isNull(p)) throw new ApiPlaneException(String.format("plugin processor [%s] does not exit.", name));
        return p;
    }

    @Override
    public Map<String, Plugin> getPlugins() {
        Map<String, Plugin> ret = new LinkedHashMap<>();
        String pluginConfig = getPluginConfig();

        ResourceGenerator rg = ResourceGenerator.newInstance(pluginConfig);
        int itemCount = rg.getValue("$.item.length()");
        for (int i = 0; i < itemCount; i++) {
            String name = rg.getValue(String.format("$.item[%s].name", i));
            String ftl = rg.getValue(String.format("$.item[%s].resource", i));
            String processor = rg.getValue(String.format("$.item[%s].processor", i));
            String description = rg.getValue(String.format("$.item[%s].description", i));
            String author = rg.getValue(String.format("$.item[%s].author", i));
            String createTime = rg.getValue(String.format("$.item[%s].createTime", i));
            String updateTime = rg.getValue(String.format("$.item[%s].updateTime", i));
            String pluginScope = rg.getValue(String.format("$.item[%s].pluginScope", i));
            String pluginPriority = rg.getValue(String.format("$.item[%s].pluginPriority", i));
            String instructionForUse = rg.getValue(String.format("$.item[%s].instructionForUse", i));
            ret.put(name, createPlugin(name, ftl, processor, description, author, createTime, updateTime, pluginScope, pluginPriority, instructionForUse));
        }
        return ret;
    }

    @Override
    public String getPluginConfig() {
        return TemplateUtils.getTemplate(PLUGIN_CONFIG, configuration).toString();
    }

    @Override
    public List<FragmentHolder> processSchema(List<String> plugins, ServiceInfo serviceInfo) {
        List<FragmentHolder> ret = new ArrayList<>();

        // 1. classify plugins
        List<PluginInstance> totalPlugin = plugins.stream().map(PluginInstance::new).collect(Collectors.toList());
        MultiValueMap<SchemaProcessor, PluginInstance> pluginMap = new LinkedMultiValueMap<>();
        totalPlugin.forEach(plugin -> pluginMap.add(getProcessor(getPlugin(plugin.getKind()).getProcessor()), plugin));

        // 2. process plugins
        Set<SchemaProcessor> processors = pluginMap.keySet();
        for (SchemaProcessor processor : processors) {
            List<PluginInstance> classifiedPlugins = pluginMap.get(processor);
            List<String> pluginStrs = classifiedPlugins.stream().map(PluginInstance::jsonString).collect(Collectors.toList());
            logger.info("process multi processor :[{}], jsons :[{}], serviceInfo :[{}]", processor.getName(), pluginStrs, serviceInfo);
            ret.addAll(processor.process(pluginStrs, serviceInfo));
        }

        return ret;
    }

    @Override
    public List<String> extractService(List<String> plugins) {
        List<String> ret = new ArrayList<>();
        plugins.stream().forEach(plugin -> {
            ResourceGenerator rg = ResourceGenerator.newInstance(plugin, ResourceType.JSON, editorContext);
            ret.addAll(rg.getValue("$.rule[*].action.target"));
            ret.addAll(rg.getValue("$.rule[*].action.pass_proxy_target[*].url"));
        });
        return ret.stream().distinct().filter(Objects::nonNull)
                .filter(item -> Pattern.compile("(.*?)\\.(.*?)\\.svc.(.*?)\\.(.*?)").matcher(item).find())
                .filter(item -> {
                    logger.info("extract service:{}", item);
                    return true;
                })
                .collect(Collectors.toList());
    }

    private SchemaProcessor getProcessor(String name) {
        logger.info("get processor:{}", name);
        Optional<SchemaProcessor> processor = processors.stream().filter(item -> item.getName().equalsIgnoreCase(name)).findAny();
        if (!processor.isPresent()) {
            throw new ApiPlaneException("can not resolve the schema processor of name:" + name);
        }
        return processor.get();
    }

    private Plugin createPlugin(
            String name,
            String ftl,
            String processor,
            String description,
            String author,
            String createTime,
            String updateTime,
            String pluginScope,
            String pluginPriority,
            String instructionForUse
    ) {
        logger.info("create plugin name:{}, ftl:{}, processor:{}, description:{}", name, ftl, processor, description);
        String schema = TemplateUtils.getTemplate(ftl, configuration).toString();

        Plugin plugin = new Plugin();
        plugin.setName(name);
        plugin.setDescription(description);
        plugin.setSchema(schema);
        plugin.setProcessor(processor);
        plugin.setAuthor(author);
        plugin.setCreateTime(createTime);
        plugin.setUpdateTime(updateTime);
        plugin.setPluginScope(pluginScope);
        plugin.setPluginPriority(pluginPriority);
        plugin.setInstructionForUse(instructionForUse);
        return plugin;
    }
}
