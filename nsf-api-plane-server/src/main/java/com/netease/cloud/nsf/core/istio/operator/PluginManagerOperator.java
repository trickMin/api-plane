package com.netease.cloud.nsf.core.istio.operator;

import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.util.function.Equals;
import me.snowdrop.istio.api.networking.v1alpha3.Plugin;
import me.snowdrop.istio.api.networking.v1alpha3.PluginManager;
import me.snowdrop.istio.api.networking.v1alpha3.PluginManagerBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/26
 **/
@Component
public class PluginManagerOperator implements IstioResourceOperator<PluginManager> {

    @Override
    public PluginManager merge(PluginManager old, PluginManager fresh) {

        PluginManager latest = new PluginManagerBuilder(old).build();

        List<Plugin> oldPlugins = old.getSpec().getPlugin();
        List<Plugin> latestPlugins = fresh.getSpec().getPlugin();
        latest.getSpec().setPlugin(mergeList(oldPlugins, latestPlugins, new PluginEquals()));
        latest.getSpec().setWorkloadLabels(fresh.getSpec().getWorkloadLabels());
        return latest;
    }

    private class PluginEquals implements Equals<Plugin> {
        @Override
        public boolean apply(Plugin op, Plugin np) {
            return Objects.equals(op.getName(), np.getName());
        }
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.PluginManager.name().equals(name);
    }

    @Override
    public boolean isUseless(PluginManager pm) {
        return pm.getSpec() == null ||
                StringUtils.isEmpty(pm.getApiVersion()) ||
                 CollectionUtils.isEmpty(pm.getSpec().getPlugin());
    }

    @Override
    public PluginManager subtract(PluginManager old, String value) {
        old.setSpec(null);
        return old;
    }
}
