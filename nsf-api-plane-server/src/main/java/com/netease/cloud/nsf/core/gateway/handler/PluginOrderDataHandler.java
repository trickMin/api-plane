package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.PluginOrder;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/27
 **/
public class PluginOrderDataHandler implements DataHandler<PluginOrder> {

    private static final String DEFAULT_PLUGIN_MANAGER_NAME = "qz-global";

    @Override
    public List<TemplateParams> handle(PluginOrder po) {

        String name = CollectionUtils.isEmpty(po.getGatewayLabels()) ?
                DEFAULT_PLUGIN_MANAGER_NAME : joinLabelMap(po.getGatewayLabels());

        TemplateParams pmParams = TemplateParams.instance()
                .put(PLUGIN_MANAGER_NAME, name)
                .put(NAMESPACE, po.getNamespace())
                .put(PLUGIN_MANAGER_WORKLOAD_LABELS, po.getGatewayLabels())
                .put(PLUGIN_MANAGER_PLUGINS, po.getPlugins());
        return Arrays.asList(pmParams);
    }

    /**
     * 将map中的key value用 "-" 连接
     * 比如 k1-v1-k2-v2
     * @param labelMap
     * @return
     */
    private String joinLabelMap(Map<String, String> labelMap) {

        List<String> labels = labelMap.entrySet().stream()
                .map(e -> e.getKey() + "-" + e.getValue())
                .collect(Collectors.toList());
        return String.join("-", labels);
    }

}
