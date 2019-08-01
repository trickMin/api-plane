package com.netease.cloud.nsf.core.operator;

import com.netease.cloud.nsf.meta.K8sResourceEnum;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.ExceptionConst;
import me.snowdrop.istio.api.networking.v1alpha3.Gateway;
import me.snowdrop.istio.api.networking.v1alpha3.Server;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 一个服务对应一个gateway,一个gateway里面只配一个server,
 * server里的hosts可以多个
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/31
 **/
public class GatewayOperator implements IstioResourceOperator<Gateway> {

    @Override
    public Gateway merge(Gateway old, Gateway fresh) {

        List<Server> oldServers = old.getSpec().getServers();
        if (CollectionUtils.isEmpty(oldServers)) {
            throw new ApiPlaneException(ExceptionConst.RESOURCE_NON_EXIST);
        }

        return null;
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.Gateway.name().equals(name);
    }
}
