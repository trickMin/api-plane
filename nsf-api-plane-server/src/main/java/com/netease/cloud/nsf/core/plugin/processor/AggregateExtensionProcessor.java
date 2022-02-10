package com.netease.cloud.nsf.core.plugin.processor;

import com.google.common.collect.Lists;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.plugin.PluginGenerator;
import com.netease.cloud.nsf.meta.ServiceInfo;
import com.netease.cloud.nsf.util.CommonUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/12/16
 **/
@Component
public class AggregateExtensionProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    private static final Logger logger = LoggerFactory.getLogger(AggregateExtensionProcessor.class);

    @Override
    public String getName() {
        return "AggregateExtensionProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator rg = PluginGenerator.newInstance(plugin);
        FragmentHolder holder;
        switch (rg.getValue("$.kind", String.class)) {
            case "rewrite":
                holder = getProcessor("RewriteProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "com.netease.rewrite");
                break;
            case "jsonp":
                holder = getProcessor("JsonpProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "com.netease.jsonp");
                break;
            case "ianus-request-transformer":
            case "transformer":
                holder = getProcessor("TransformProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "com.netease.transformation");
                break;
            case "body-extractor":
                //todo: encapsulated
                holder = getProcessor("DefaultProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "com.netease.bodyextractor");
                break;
            case "auth":
                //todo: encapsulated
                holder = getProcessor("DefaultProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "envoy.ext_authz");
                break;
            case "static-downgrade":
                holder = getProcessor("StaticDowngradeProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "com.netease.staticdowngrade");
                break;
            case "dynamic-downgrade":
                holder = getProcessor("DynamicDowngradeProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "com.netease.dynamicdowngrade");
                break;
            case "ianus-rate-limiting":
                holder = getProcessor("RateLimitProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "envoy.ratelimit");
                break;
            case "mesh-rate-limiting":
                holder = getProcessor("MeshRateLimitProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "envoy.ratelimit");
                break;
            case "ianus-percent-limit":
                holder = getProcessor("FlowLimitProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "envoy.fault");
                break;
            case "ip-restriction":
                holder = getProcessor("IpRestrictionProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "com.netease.iprestriction");
                break;
            case "ua-restriction":
                holder = getProcessor("UaRestrictionProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "proxy.filters.http.ua_restriction");
                break;
            case "referer-restriction":
                holder = getProcessor("RefererRestrictionProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "proxy.filters.http.referer_restriction");
                break;
            case "header-restriction":
                holder = getProcessor("HeaderRestrictionProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "proxy.filters.http.header_restriction");
                break;
            case "response-header-rewrite":
                holder = getProcessor("ResponseHeaderRewriteProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "com.netease.filters.http.header_rewrite");
                break;
            case "cors":
                holder = getProcessor("CorsProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "envoy.cors");
                break;
            case "cache":
                holder = getProcessor("Cache").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "com.netease.supercache");
                break;
            case "local-cache":
                holder = getProcessor("LocalCache").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "proxy.filters.http.local_cache");
                break;
            case "redis-cache":
                holder = getProcessor("RedisCache").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "proxy.filters.http.redis_cache");
                break;
            case "sign-auth": case "jwt-auth": case "oauth2-auth":
                holder = getProcessor("SuperAuth").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "com.netease.superauthz");
                break;
            case "request-transformer":
                holder = getProcessor("DefaultProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "com.netease.transformation");
                break;
            case "circuit-breaker":
                holder = getProcessor("CircuitBreakerProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "com.netease.circuitbreaker");
                break;
            case "function":
                holder = getProcessor("FunctionProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "envoy.lua");
                break;
            case "local-limiting":
                holder = getProcessor("LocalLimitProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "com.netease.filters.http.locallimit");
                break;
            case "soap-json-transcoder":
                holder = getProcessor("SoapJsonTranscoderProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "com.netease.filters.http.soapjsontranscoder");
                break;
            case "trace":
            default:
                holder = getProcessor("RestyProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "com.netease.resty");
                break;
        }
        return holder;
    }

    private void coverToExtensionPlugin(FragmentHolder holder, String name) {
        if (Objects.nonNull(holder.getVirtualServiceFragment())) {
            PluginGenerator source = PluginGenerator.newInstance(holder.getVirtualServiceFragment().getContent(), ResourceType.YAML);
            PluginGenerator builder = PluginGenerator.newInstance(String.format("{\"name\":\"%s\"}", name));
            builder.createOrUpdateJson("$", "settings", source.jsonString());
            holder.getVirtualServiceFragment().setContent(builder.yamlString());
            logger.info("Extension plugin: [{}]", builder.yamlString());
        }
    }

    @Override
    public List<FragmentHolder> process(List<String> plugins, ServiceInfo serviceInfo) {
        List<FragmentHolder> holders = Lists.newArrayList();
        List<String> luaPlugins = plugins.stream().filter(CommonUtil::isLuaPlugin).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(luaPlugins)) {
            List<FragmentHolder> luaHolder = getProcessor("RestyProcessor").process(luaPlugins, serviceInfo);
            holders.addAll(luaHolder);
        }

        List<String> notLuaPlugins = plugins.stream().filter(item -> !CommonUtil.isLuaPlugin(item)).collect(Collectors.toList());

        List<FragmentHolder> notLuaHolder = notLuaPlugins.stream()
                .map(plugin -> process(plugin, serviceInfo))
                .collect(Collectors.toList());
        holders.addAll(notLuaHolder);

        // 根据租户将插件分类
        MultiValueMap<String, FragmentWrapper> xUserMap = new LinkedMultiValueMap<>();
        // 一个租户下最多配置一个限流插件
        Map<String, FragmentWrapper> sharedConfigMap = new LinkedHashMap<>();
        Map<String, FragmentWrapper> smartLimiterMap = new LinkedHashMap<>();
        holders.forEach(holder -> {
            FragmentWrapper wrapper = holder.getVirtualServiceFragment();
            FragmentWrapper sharedConfig = holder.getSharedConfigFragment();
            FragmentWrapper smartLimiter = holder.getSmartLimiterFragment();
            if (wrapper == null) return;
            String xUserId = wrapper.getXUserId();
            String xUser;
            if (StringUtils.isEmpty(xUserId)) {
                xUser = "NoneUser";
            } else {
                xUser = xUserId;
            }
            xUserMap.add(xUser, wrapper);
            if (Objects.nonNull(sharedConfig)) {
                sharedConfigMap.put(xUser, wrapper);
            }
            if (Objects.nonNull(smartLimiter)) {
                smartLimiterMap.put(xUser, wrapper);
            }

        });
        List<FragmentHolder> ret = new ArrayList<>();
        for (Map.Entry<String, List<FragmentWrapper>> userMap : xUserMap.entrySet()) {
            PluginGenerator builder = PluginGenerator.newInstance("{\"ext\":[]}");
            for (FragmentWrapper wrapper : userMap.getValue()) {
                PluginGenerator source = PluginGenerator.newInstance(wrapper.getContent(), ResourceType.YAML);
                builder.addJsonElement("$.ext", source.jsonString());
            }
            String xUserId = "NoneUser".equals(userMap.getKey()) ? null : userMap.getKey();
            FragmentHolder holder = new FragmentHolder();
            FragmentWrapper wrapper = new FragmentWrapper.Builder()
                    .withContent(builder.yamlString())
                    .withResourceType(K8sResourceEnum.VirtualService)
                    .withFragmentType(FragmentTypeEnum.VS_API)
                    .withXUserId(xUserId)
                    .build();
            holder.setVirtualServiceFragment(wrapper);
            if (sharedConfigMap.containsKey(userMap.getKey())) {
                holder.setSharedConfigFragment(sharedConfigMap.get(userMap.getKey()));
            }
            if (smartLimiterMap.containsKey(userMap.getKey())) {
                holder.setSmartLimiterFragment(smartLimiterMap.get(userMap.getKey()));
            }
            ret.add(holder);
        }
        return ret;
    }
}
