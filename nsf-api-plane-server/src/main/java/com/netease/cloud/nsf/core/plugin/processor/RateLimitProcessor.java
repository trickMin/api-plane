package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.meta.ServiceInfo;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/22
 **/
@Component
public class RateLimitProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "RateLimitProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        //todo: XUser
        FragmentHolder holder = new FragmentHolder();
        ResourceGenerator total = ResourceGenerator.newInstance(plugin, ResourceType.JSON, editorContext);
        String xUserId = getAndDeleteXUserId(total);

        List<Object> limits = total.getValue("$.limit_by_list");

        ResourceGenerator rateLimitGen = ResourceGenerator.newInstance("{\"ratelimit\":{\"rateLimits\":[]}}");
        ResourceGenerator shareConfigGen = ResourceGenerator.newInstance("[{\"domain\":\"qingzhou\",\"descriptors\":[]}]");

        limits.forEach(limit -> {
            ResourceGenerator rg = ResourceGenerator.newInstance(limit, ResourceType.OBJECT, editorContext);
            // 频控计算的不同维度，例如second, minute, hour, day(month, year暂时不支持)
            getUnits(rg).forEach((unit, duration) -> {
                String headerDescriptor = getHeaderDescriptor(serviceInfo, xUserId);
                rateLimitGen.addJsonElement("$.ratelimit.rateLimits", createRateLimits(rg, serviceInfo, headerDescriptor, null));
                shareConfigGen.addJsonElement("$[0].descriptors", createShareConfig(rg, serviceInfo, headerDescriptor, unit, duration));
            });
        });
        holder.setSharedConfigFragment(
                new FragmentWrapper.Builder()
                        .withXUserId(xUserId)
                        .withFragmentType(FragmentTypeEnum.SHARECONFIG)
                        .withResourceType(K8sResourceEnum.SharedConfig)
                        .withContent(shareConfigGen.yamlString())
                        .build()
        );
        holder.setVirtualServiceFragment(
                new FragmentWrapper.Builder()
                        .withXUserId(xUserId)
                        .withFragmentType(FragmentTypeEnum.VS_API)
                        .withResourceType(K8sResourceEnum.VirtualService)
                        .withContent(rateLimitGen.yamlString())
                        .build()
        );
        return holder;
    }

    private String createRateLimits(ResourceGenerator rg, ServiceInfo serviceInfo, String headerDescriptor, String xUserId) {
        ResourceGenerator vs = ResourceGenerator.newInstance("{\"stage\":0,\"actions\":[]}");

        vs.addJsonElement("$.actions",
                String.format("{\"headerValueMatch\":{\"headers\":[],\"descriptorValue\":\"%s\"}}", headerDescriptor));
        vs.addJsonElement("$.actions[0].headerValueMatch.headers",
                String.format("{\"name\":\":path\",\"regexMatch\":\"%s\"}", serviceInfo.getUri()));
        vs.addJsonElement("$.actions[0].headerValueMatch.headers",
                String.format("{\"name\":\":authority\",\"regexMatch\":\"%s\"}", serviceInfo.getHosts()));
        vs.addJsonElement("$.actions[0].headerValueMatch.headers",
                String.format("{\"name\":\":method\",\"regexMatch\":\"%s\"}", serviceInfo.getMethod()));

        int length = 0;
        if (rg.contain("$.pre_condition")) {
            length = rg.getValue("$.pre_condition.length()");
        }
        if (length != 0) {
            String matchHeader = getMatchHeader(rg);
            for (int i = 0; i < length; i++) {
                String operator = rg.getValue(String.format("$.pre_condition[%d].operator", i));
                String rightValue = rg.getValue(String.format("$.pre_condition[%d].right_value", i));
                String regex;
                switch (operator) {
                    case "≈":
                        regex = escapeBackSlash(rightValue);
                        break;
                    case "!≈":
                        regex = String.format("((?!%s).)*", escapeBackSlash(rightValue));
                        break;
                    case "=":
                        regex = String.format("%s", escapeExprSpecialWord(rightValue));
                        break;
                    case "!=":
                        regex = String.format("((?!%s).)*", escapeExprSpecialWord(rightValue));
                        break;
                    default:
                        throw new ApiPlaneException(String.format("Unsupported $.config.limit_by_list.pre_condition.operator: %s", operator));
                }

                vs.addJsonElement("$.actions[0].headerValueMatch.headers",
                        String.format("{\"name\":\"%s\",\"regexMatch\":\"%s\"}", matchHeader, regex));
            }
        }
        if (length == 0 && rg.contain("$.identifier_extractor") && !StringUtils.isEmpty(rg.getValue("$.identifier_extractor", String.class))) {
            String matchHeader = getMatchHeader(rg);
            String descriptorKey = String.format("WithoutValueHeader[%s]", matchHeader);
            vs.addJsonElement("$.actions", String.format("{\"requestHeaders\":{\"headerName\":\"%s\",\"descriptorKey\":\"%s\"}}", matchHeader, descriptorKey));
        }
        return vs.jsonString();
    }

    private String createShareConfig(ResourceGenerator rg, ServiceInfo serviceInfo, String headerDescriptor, String unit, Long duration) {
        ResourceGenerator shareConfig;
        int length = 0;
        if (rg.contain("$.pre_condition")) {
            length = rg.getValue("$.pre_condition.length()");
        }
        if (length == 0 && rg.contain("$.identifier_extractor") && !StringUtils.isEmpty(rg.getValue("$.identifier_extractor", String.class))) {
            String matchHeader = getMatchHeader(rg);
            String descriptorKey = String.format("WithoutValueHeader[%s]", matchHeader);
            shareConfig = ResourceGenerator.newInstance(String.format("{\"api\":\"%s\",\"key\":\"header_match\",\"value\":\"%s\",\"descriptors\":[{\"key\":\"%s\",\"rate_limit\":{\"unit\":\"%s\",\"requests_per_unit\":\"%d\"}}]}",
                    getApiName(serviceInfo),
                    headerDescriptor,
                    descriptorKey,
                    unit,
                    duration
            ));
        } else {
            shareConfig = ResourceGenerator.newInstance(String.format("{\"key\":\"header_match\",\"value\":\"%s\",\"rate_limit\":{\"unit\":\"%s\",\"requests_per_unit\":%d}}",
                    headerDescriptor,
                    unit,
                    duration
            ));
        }
        return shareConfig.jsonString();
    }

    private String getMatchHeader(ResourceGenerator rg) {
        if (!rg.contain("$.identifier_extractor")) return "";
        String extractor = rg.getValue("$.identifier_extractor");

        String matchHeader;
        Matcher matcher = Pattern.compile("Header\\[(.*)\\]").matcher(extractor);
        if (matcher.find()) {
            matchHeader = matcher.group(1);
        } else {
            throw new ApiPlaneException(String.format("Unsupported $.config.limit_by_list.identifier_extractor: %s", extractor));
        }
        return matchHeader;
    }

    private Map<String, Long> getUnits(ResourceGenerator rg) {
        Map<String, Long> ret = new LinkedHashMap<>();
        String[][] map = new String[][]{
                {"$.second", "SECOND"},
                {"$.minute", "MINUTE"},
                {"$.hour", "HOUR"},
                {"$.day", "DAY"}
        };
        for (String[] obj : map) {
            if (rg.contain(obj[0])) {
                ret.put(obj[1], rg.getValue(obj[0], Long.class));
            }
        }
        return ret;
    }

    private String getHeaderDescriptor(ServiceInfo serviceInfo, String user) {
        if (StringUtils.isBlank(user)) {
            user = "none";
        }
        return String.format("Service[%s]-User[%s]-Api[%s]-Id[%s]", getServiceName(serviceInfo), user, getApiName(serviceInfo), UUID.randomUUID().toString());
    }
}
