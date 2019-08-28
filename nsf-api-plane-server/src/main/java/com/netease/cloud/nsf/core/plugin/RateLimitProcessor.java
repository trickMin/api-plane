package com.netease.cloud.nsf.core.plugin;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.meta.ServiceInfo;
import com.netease.cloud.nsf.util.K8sResourceEnum;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/22
 **/
@Component
public class RateLimitProcessor extends AbstractYxSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "RateLimitProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        FragmentHolder holder = new FragmentHolder();
        ResourceGenerator total = ResourceGenerator.newInstance(plugin, ResourceType.JSON, editorContext);
        List<Object> limits = total.getValue("$.config.limit_by_list");
        AtomicInteger headerNo = new AtomicInteger(0);

        ResourceGenerator rateLimitGen = ResourceGenerator.newInstance("{\"rate_limits\":[]}");
        ResourceGenerator shareConfigGen = ResourceGenerator.newInstance("{\"domain\":\"qingzhou\",\"descriptors\":[]}");

        limits.forEach(limit -> {
            ResourceGenerator rg = ResourceGenerator.newInstance(limit, ResourceType.OBJECT, editorContext);
            // 频控计算的不同维度，例如second, minute, hour, day(month, year暂时不支持)
            Integer no = headerNo.getAndIncrement();
            getUnits(rg).forEach((unit, duration) -> {
                String headerDescriptor = getHeaderDescriptor(no, getMatchHeader(rg), unit);
                rateLimitGen.addJsonElement("$.rate_limits", createRateLimits(rg, serviceInfo, headerDescriptor));
                shareConfigGen.addJsonElement("$.descriptors", createShareConfig(serviceInfo, headerDescriptor, unit, duration));
            });

        });
        holder.setSharedConfigFragment(
                new FragmentWrapper.Builder()
                        .withFragmentType(FragmentTypeEnum.NEW_DESCRIPTORS)
                        .withResourceType(K8sResourceEnum.SharedConfig)
                        .withContent(shareConfigGen.yamlString())
                        .build()
        );
        holder.setVirtualServiceFragment(
                new FragmentWrapper.Builder()
                        .withFragmentType(FragmentTypeEnum.DEFAULT_MATCH)
                        .withResourceType(K8sResourceEnum.VirtualService)
                        .withContent(rateLimitGen.yamlString())
                        .build()
        );
        return holder;
    }


    private String createRateLimits(ResourceGenerator rg, ServiceInfo serviceInfo, String headerDescriptor) {
        ResourceGenerator vs = ResourceGenerator.newInstance(String.format("{\"stage\":0,\"action\":[{\"generic_key\":{\"descriptor_value\":\"%s\"}}]}", getApiName(serviceInfo)));

        String matchHeader = getMatchHeader(rg);

        //todo: if !rg.contain($.pre_condition)
        if (rg.contain("$.pre_condition")) {
            vs.addJsonElement("$.action",
                    String.format("{\"header_value_match\":{\"headers\":[],\"descriptor_value\":\"%s\"}}", headerDescriptor));

            int length = rg.getValue("$.pre_condition.length()");
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

                vs.addJsonElement("$.action[1].header_value_match.headers",
                        String.format("{\"name\":\"%s\",\"regex_match\":\"%s\"}", matchHeader, regex));
            }
        }
        return vs.jsonString();
    }

    private String createShareConfig(ServiceInfo serviceInfo, String headerDescriptor, String unit, Integer duration) {
        ResourceGenerator shareConfig = ResourceGenerator.newInstance(String.format("{\"key\":\"generic_key\",\"value\":\"%s\",\"descriptors\":[{\"key\":\"header_match\",\"value\":\"%s\",\"rate_limit\":{\"unit\":\"%s\",\"requests_per_unit\":%d}}]}",
                getApiName(serviceInfo),
                headerDescriptor,
                unit,
                duration
        ));
        return shareConfig.jsonString();
    }

    private String getMatchHeader(ResourceGenerator rg) {
        String extractor = rg.getValue("$.identifier_extractor");

        String matchHeader;
        Matcher matcher = Pattern.compile("Header\\[(.*)\\]").matcher(extractor);
        if ("Ip".equalsIgnoreCase(extractor)) {
            matchHeader = "X-Forwarded-For";
        } else if (matcher.find()) {
            matchHeader = matcher.group(1);
        } else {
            throw new ApiPlaneException(String.format("Unsupported $.config.limit_by_list.identifier_extractor: %s", extractor));
        }
        return matchHeader;
    }

    private Map<String, Integer> getUnits(ResourceGenerator rg) {
        Map<String, Integer> ret = new LinkedHashMap<>();
        String[][] map = new String[][]{
                {"$.second", "SECOND"},
                {"$.minute", "MINUTE"},
                {"$.hour", "HOUR"},
                {"$.day", "DAY"}
        };
        for (String[] obj : map) {
            if (rg.contain(obj[0])) {
                ret.put(obj[1], rg.getValue(obj[0]));
            }
        }
        return ret;
    }

    private String getHeaderDescriptor(Integer no, String headerName, String unit) {
        return String.format("Header[%s][%s][%d]", headerName, unit, no);
    }
}
