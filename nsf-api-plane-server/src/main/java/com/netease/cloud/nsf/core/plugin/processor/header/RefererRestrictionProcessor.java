package com.netease.cloud.nsf.core.plugin.processor.header;

import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.springframework.stereotype.Component;
@Component
public class RefererRestrictionProcessor extends HeaderRestrictionProcessor {


    @Override
    public String getName() {
        return "RefererRestrictionProcessor";
    }


    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        setPluginHeader("Referer");
        return super.process(plugin, serviceInfo);
    }

}
