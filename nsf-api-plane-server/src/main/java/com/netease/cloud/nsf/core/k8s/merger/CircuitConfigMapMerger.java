package com.netease.cloud.nsf.core.k8s.merger;

import com.netease.cloud.nsf.util.function.Merger;
import me.snowdrop.istio.api.networking.v1alpha3.GatewayPlugin;
import me.snowdrop.istio.api.networking.v1alpha3.Plugins;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhangzihao
 */
public class CircuitConfigMapMerger implements Merger<GatewayPlugin> {

    private String pluginName;

    public CircuitConfigMapMerger(String pluginName) {
        this.pluginName = pluginName;
    }

    @Override
    public GatewayPlugin merge(GatewayPlugin old, GatewayPlugin latest) {

        List<Plugins> toUpdate = extractPlugin(latest);
        List<Plugins> current = subtractPlugin(old);
        toUpdate.addAll(current);
        if (CollectionUtils.isEmpty(toUpdate)){
            latest.setSpec(null);
        }else {
            latest.getSpec().setPlugins(toUpdate);
        }
        return latest;
    }

    private List<Plugins> extractPlugin(GatewayPlugin gatewayPlugin) {

        List<Plugins> plugins = gatewayPlugin.getSpec().getPlugins();
        if (CollectionUtils.isEmpty(plugins)) {
            return new ArrayList<>();
        }
        return plugins.stream()
                .filter(p -> p.getName().equals(pluginName))
                .collect(Collectors.toList());
    }

    private List<Plugins> subtractPlugin(GatewayPlugin gatewayPlugin) {
        List<Plugins> plugins = gatewayPlugin.getSpec().getPlugins();
        if (CollectionUtils.isEmpty(plugins)) {
            return new ArrayList<>();
        }
        return plugins.stream()
                .filter(p -> !p.getName().equals(pluginName))
                .collect(Collectors.toList());

    }
}
