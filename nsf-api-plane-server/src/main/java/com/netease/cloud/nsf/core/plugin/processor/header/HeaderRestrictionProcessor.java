package com.netease.cloud.nsf.core.plugin.processor.header;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.plugin.PluginGenerator;
import com.netease.cloud.nsf.core.plugin.processor.AbstractSchemaProcessor;
import com.netease.cloud.nsf.meta.ServiceInfo;
import net.minidev.json.JSONArray;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author zhufengwei.sx
 * @date 2021/8/6 9:56
 * 基于Http header进行黑白名单过滤的插件
 */

@Component
public class HeaderRestrictionProcessor extends AbstractSchemaProcessor {

    @Override
    public String getName() {
        return "HeaderRestrictionProcessor";
    }

    protected String header;

    /**
     * @link https://g.hz.netease.com/qingzhou/envoy-netease/-/blob/release-20211030/api/proxy/filters/http/header_restriction/v2/header_restriction.proto
     * yaml格式
     * plugins:
     *   - name: proxy.filters.http.ua_restriction
     *     settings:
     *       config:
     *         list:
     *         - headers:
     *           - name: User-Agent
     *             safe_regex_match:
     *               google_re2: {}
     *               regex: (python|curl|java|wget|httpclient|okhttp)
     *         - headers:
     *           - exact_match: test
     *             name: User-Agent
     *         type: BLACK
     */
    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator rg = PluginGenerator.newInstance(plugin);
        PluginGenerator ret = PluginGenerator.newInstance("{\"list\":[]}");
        JsonArray jsonArray = new JsonParser().parse(rg.getValue("$.list[*]", JSONArray.class).toJSONString()).getAsJsonArray();
        for(JsonElement jsonElement : jsonArray){
            JsonObject matchCondition = jsonElement.getAsJsonObject();
            String matchType = matchCondition.get("match_type").getAsString();
            String headerType = null == header ? matchCondition.get("header").getAsString() : header;
            JsonArray matchValues = matchCondition.getAsJsonArray("value");
            for (JsonElement matchValue : matchValues) {
                PluginGenerator builder = PluginGenerator.newInstance("{}");
                builder.createOrUpdateJson("$", "name", headerType);
                String matchValueStr = matchValue.getAsString();
                if ("safe_regex_match".equals(matchType)){
                    matchValueStr = PluginGenerator.newInstance("{}")
                            .createOrUpdateJson("$","google_re2", "{}")
                            .createOrUpdateJson("$","regex", matchValueStr).jsonString();
                }
                builder.createOrUpdateJson("$", matchType, matchValueStr);
                ret.addJsonElement("$.list", PluginGenerator.newInstance("{\"headers\":[]}")
                        .addJsonElement("$.headers", builder.jsonString()).jsonString());
            }
        }
        if (Objects.equals("0", rg.getValue("$.type", String.class))) {
            ret.createOrUpdateJson("$", "type", "BLACK");
        } else if (Objects.equals("1", rg.getValue("$.type", String.class))) {
            ret.createOrUpdateJson("$", "type", "WHITE");
        }
        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withXUserId(getAndDeleteXUserId(rg))
                .withFragmentType(FragmentTypeEnum.VS_API)
                .withResourceType(K8sResourceEnum.VirtualService)
                .withContent(PluginGenerator.newInstance("{\"config\":{}}").createOrUpdateJson("$", "config", ret.jsonString()).yamlString())
                .build();
        fragmentHolder.setVirtualServiceFragment(wrapper);
        return fragmentHolder;
    }


    protected void setPluginHeader(String header){
        this.header = header;
    };
}

