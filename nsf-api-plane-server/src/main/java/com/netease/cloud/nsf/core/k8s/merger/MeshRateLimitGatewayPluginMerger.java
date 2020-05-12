package com.netease.cloud.nsf.core.k8s.merger;

import com.netease.cloud.nsf.core.k8s.K8sConst;
import com.netease.cloud.nsf.core.k8s.subtracter.MeshRateLimitGatewayPluginSubtracter;
import com.netease.cloud.nsf.util.function.Merger;
import me.snowdrop.istio.api.networking.v1alpha3.GatewayPlugin;
import me.snowdrop.istio.api.networking.v1alpha3.Plugins;
import org.springframework.util.CollectionUtils;

import java.util.Optional;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/13
 **/
public class MeshRateLimitGatewayPluginMerger implements Merger<GatewayPlugin> {

    /**
     *
     * @param old
     * @param latest 不考虑带其他插件的情况，只考虑生成的只带限流插件
     * @return
     */
    @Override
    public GatewayPlugin merge(GatewayPlugin old, GatewayPlugin latest) {

        //新的为空，则清空老的
        if (isNull(latest)) {
            return new MeshRateLimitGatewayPluginSubtracter().subtract(old);
        }
        if (isNull(old)) return latest;

        Optional<Plugins> oldRateLimit = old.getSpec().getPlugins().stream().filter(p -> K8sConst.RATE_LIMIT_PLUGIN_NAME.equals(p.getName())).findFirst();
        Optional<Plugins> latestRateLimit = latest.getSpec().getPlugins().stream().filter(p -> K8sConst.RATE_LIMIT_PLUGIN_NAME.equals(p.getName())).findFirst();

        if (!latestRateLimit.isPresent()) {
            old.getSpec().getPlugins().remove(oldRateLimit.get());
            return old;
        }
        if (!oldRateLimit.isPresent()) {
            old.getSpec().getPlugins().add(latestRateLimit.get());
            return old;
        }
        //override old
        oldRateLimit.get().setSettings(latestRateLimit.get().getSettings());
        old.getSpec().setService(latest.getSpec().getService());
        return old;
    }

    private boolean isNull(GatewayPlugin gp) {
        return gp == null ||
                gp.getSpec() == null ||
                CollectionUtils.isEmpty(gp.getSpec().getPlugins());
    }
}
