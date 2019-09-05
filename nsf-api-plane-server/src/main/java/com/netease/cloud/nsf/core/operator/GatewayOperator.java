package com.netease.cloud.nsf.core.operator;

import com.netease.cloud.nsf.util.K8sResourceEnum;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.ExceptionConst;
import me.snowdrop.istio.api.networking.v1alpha3.Gateway;
import me.snowdrop.istio.api.networking.v1alpha3.GatewayBuilder;
import me.snowdrop.istio.api.networking.v1alpha3.Server;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * 一个服务对应一个gateway,一个gateway里面只配一个server,
 * server里的hosts可以多个
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/31
 **/
@Component
public class GatewayOperator implements IstioResourceOperator<Gateway> {

    @Override
    public Gateway merge(Gateway old, Gateway fresh) {

        List<Server> oldServers = old.getSpec().getServers();
        if (CollectionUtils.isEmpty(oldServers)) {
            throw new ApiPlaneException(ExceptionConst.RESOURCE_NON_EXIST);
        }

        Gateway latestGateway = new GatewayBuilder(old).build();

        Server firstOldServer = oldServers.get(0);
        List<String> oldHosts = firstOldServer.getHosts();

        List<Server> freshServers = fresh.getSpec().getServers();
        if (CollectionUtils.isEmpty(freshServers)) {
            return old;
        }
        Server firstFreshServer = freshServers.get(0);
        List<String> freshHosts = firstFreshServer.getHosts();
        if (CollectionUtils.isEmpty(freshHosts)) {
            return old;
        }

        Server firstLatestServer = latestGateway.getSpec().getServers().get(0);
        firstLatestServer.setHosts(mergeList(oldHosts, freshHosts, (ot, nt) -> Objects.equals(ot, nt)));
        return latestGateway;
    }



    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.Gateway.name().equals(name);
    }

    @Override
    public boolean isUseless(Gateway gateway) {
        return gateway == null ||
                gateway.getSpec() == null ||
                 CollectionUtils.isEmpty(gateway.getSpec().getServers());
    }
}
