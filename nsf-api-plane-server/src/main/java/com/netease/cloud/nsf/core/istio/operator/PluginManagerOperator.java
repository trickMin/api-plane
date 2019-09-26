package com.netease.cloud.nsf.core.istio.operator;

import com.netease.cloud.nsf.util.K8sResourceEnum;
import me.snowdrop.istio.api.networking.v1alpha3.PluginManager;
import me.snowdrop.istio.api.networking.v1alpha3.PluginManagerBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/26
 **/
@Component
public class PluginManagerOperator implements IstioResourceOperator<PluginManager> {

    @Override
    public PluginManager merge(PluginManager old, PluginManager fresh) {

        PluginManager latest = new PluginManagerBuilder(old).build();
        latest.getSpec().setPlugins(fresh.getSpec().getPlugins());

        return latest;
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.PluginManager.name().equals(name);
    }

    @Override
    public boolean isUseless(PluginManager pm) {
        return pm.getSpec() == null ||
                CollectionUtils.isEmpty(pm.getSpec().getPlugins());
    }
}
