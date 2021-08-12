package com.netease.cloud.nsf.core.plugin.processor.header;

import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.meta.ServiceInfo;

public class UaRestricionProcessor extends HeaderRestrictionProcessor{
    @Override
    public String getName() {
        return "UaRestricionProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        setPluginHeader("UserAgent");
        return super.process(plugin, serviceInfo);
    }
}
