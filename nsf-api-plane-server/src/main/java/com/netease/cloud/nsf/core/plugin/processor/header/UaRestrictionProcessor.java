package com.netease.cloud.nsf.core.plugin.processor.header;

import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.springframework.stereotype.Component;

@Component
public class UaRestrictionProcessor extends HeaderRestrictionProcessor{

    @Override
    public String getName() {
        return "UaRestrictionProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        setPluginHeader("UserAgent");
        return super.process(plugin, serviceInfo);
    }
}
