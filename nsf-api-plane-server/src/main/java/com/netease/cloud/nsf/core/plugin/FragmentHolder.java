package com.netease.cloud.nsf.core.plugin;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/15
 **/
public class FragmentHolder {
    private FragmentWrapper virtualServiceFragment;

    private FragmentWrapper destinationRuleFragment;

    private FragmentWrapper gatewayFragment;

    public FragmentWrapper getVirtualServiceFragment() {
        return virtualServiceFragment;
    }

    public void setVirtualServiceFragment(FragmentWrapper virtualServiceFragment) {
        this.virtualServiceFragment = virtualServiceFragment;
    }

    public FragmentWrapper getDestinationRuleFragment() {
        return destinationRuleFragment;
    }

    public void setDestinationRuleFragment(FragmentWrapper destinationRuleFragment) {
        this.destinationRuleFragment = destinationRuleFragment;
    }

    public FragmentWrapper getGatewayFragment() {
        return gatewayFragment;
    }

    public void setGatewayFragment(FragmentWrapper gatewayFragment) {
        this.gatewayFragment = gatewayFragment;
    }
}
