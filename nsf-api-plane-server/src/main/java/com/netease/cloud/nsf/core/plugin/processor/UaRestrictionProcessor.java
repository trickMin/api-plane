package com.netease.cloud.nsf.core.plugin.processor;

import com.mysql.jdbc.StringUtils;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.plugin.PluginGenerator;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author zhufengwei.sx
 * @date 2021/8/6 12:46
 */
@Component
public class UaRestrictionProcessor extends HeadRestrictionProcessor {
    @Override
    public String getName() {
        return "UaRestrictionProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator rg = PluginGenerator.newInstance(plugin);
        rg.createOrUpdateJson("$", "kind","head-restriction");
        rg.createOrUpdateJson("$", "name","user-agent");
        return super.process(rg.jsonString(), serviceInfo);
    }
}
