package com.netease.cloud.nsf.core.gateway;

import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/8/28
 **/
public class RawResourceContainer {

    List<FragmentWrapper> virtualServices = new ArrayList<>();
    List<FragmentWrapper> destinationRules = new ArrayList<>();
    List<FragmentWrapper> gateways = new ArrayList<>();
    List<FragmentWrapper> sharedConfigs = new ArrayList<>();

    public void add(FragmentHolder holder) {

        if (holder == null) return;

        if (holder.getVirtualServiceFragment() != null) {
            virtualServices.add(holder.getVirtualServiceFragment());
        }
        if (holder.getDestinationRuleFragment() != null) {
            destinationRules.add(holder.getDestinationRuleFragment());
        }
        if (holder.getGatewayFragment() != null) {
            gateways.add(holder.getGatewayFragment());
        }
        if (holder.getSharedConfigFragment() != null) {
            sharedConfigs.add(holder.getSharedConfigFragment());
        }
    }

    public void add(List<FragmentHolder> holders) {
        if (CollectionUtils.isEmpty(holders)) return;
        holders.stream().forEach(h -> add(h));
    }

    public List<FragmentWrapper> getVirtualServices() {
        return virtualServices;
    }

    public List<FragmentWrapper> getDestinationRules() {
        return destinationRules;
    }

    public List<FragmentWrapper> getGateways() {
        return gateways;
    }

    public List<FragmentWrapper> getSharedConfigs() {
        return sharedConfigs;
    }
}
