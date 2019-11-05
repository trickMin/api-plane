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
        logger.info("get plugin {} :{}", name, p);
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
            ret.put(name, createPlugin(name, ftl, processor, description));
        }
        return ret;
    }

    @Override
    public String getPluginConfig() {
        String pluginConfig = TemplateUtils.getTemplate(PLUGIN_CONFIG, configuration).toString();
        logger.info("get plugin config: \n{}", pluginConfig);
        return pluginConfig;
    }

    @Override
    public List<FragmentHolder> processSchema(List<String> plugins, ServiceInfo serviceInfo) {
        List<FragmentHolder> ret = new ArrayList<>();

        // 1. classify plugins
        List<PluginInstance> totalPlugin = plugins.stream().map(PluginInstance::new).collect(Collectors.toList());
        MultiValueMap<String, PluginInstance> pluginMap = new LinkedMultiValueMap<>();
        totalPlugin.forEach(plugin -> pluginMap.add(plugin.getKind(), plugin));

        // 2. process plugins
        Set<String> kinds = pluginMap.keySet();
        for (String kind : kinds) {
            List<PluginInstance> classifiedPlugins = pluginMap.get(kind);
            List<String> pluginStrs = classifiedPlugins.stream().map(PluginInstance::jsonString).collect(Collectors.toList());
            Plugin p = getPlugin(kind);
            logger.info("process multi plugin :[{}], jsons :[{}], serviceInfo :[{}]", p, pluginStrs, serviceInfo);
            ret.addAll(getProcessor(p.getProcessor()).process(pluginStrs, serviceInfo));
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
        return processors.stream().filter(item -> item.getName().equalsIgnoreCase(name)).findAny().get();
    }

    private Plugin createPlugin(String name, String ftl, String processor, String description) {
        logger.info("create plugin name:{}, ftl:{}, processor:{}, description:{}", name, ftl, processor, description);
        String schema = TemplateUtils.getTemplate(ftl, configuration).toString();

        Plugin plugin = new Plugin();
        plugin.setName(name);
        plugin.setDescription(description);
        plugin.setSchema(schema);
        plugin.setProcessor(processor);
        return plugin;
    }
}
