package com.netease.cloud.nsf.core.plugin;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/15
 **/
public class FragmentHolder {
    private FragmentWrapper virtualServiceFragment;

    private FragmentWrapper sharedConfigFragment;

    private FragmentWrapper gatewayPluginsFragment;

    public FragmentWrapper getVirtualServiceFragment() {
        return virtualServiceFragment;
    }

    public void setVirtualServiceFragment(FragmentWrapper virtualServiceFragment) {
        this.virtualServiceFragment = virtualServiceFragment;
    }

    public FragmentWrapper getSharedConfigFragment() {
        return sharedConfigFragment;
    }

    public void setSharedConfigFragment(FragmentWrapper sharedConfigFragment) {
        this.sharedConfigFragment = sharedConfigFragment;
    }

    public FragmentWrapper getGatewayPluginsFragment() {
        return gatewayPluginsFragment;
    }

    public void setGatewayPluginsFragment(FragmentWrapper gatewayPluginsFragment) {
        this.gatewayPluginsFragment = gatewayPluginsFragment;
    }
}
