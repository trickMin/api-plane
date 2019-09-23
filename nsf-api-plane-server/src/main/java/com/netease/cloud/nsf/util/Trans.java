package com.netease.cloud.nsf.util;

import com.netease.cloud.nsf.meta.API;
import com.netease.cloud.nsf.meta.ApiOption;
import com.netease.cloud.nsf.meta.Service;
import com.netease.cloud.nsf.meta.UriMatch;
import com.netease.cloud.nsf.meta.web.PortalAPI;
import com.netease.cloud.nsf.meta.web.PortalService;
import com.netease.cloud.nsf.meta.web.YxAPI;
import org.springframework.beans.BeanUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/19
 **/
public class Trans {


    public static API yxAPI2API(YxAPI yxApi) {
        API api = new API();

        ApiOption option = yxApi.getOption();
        // FIXME
        BeanUtils.copyProperties(yxApi, api);
        api.setUriMatch(UriMatch.get(yxApi.getUriMatch()));
        api.setRetries(option.getRetries());
        api.setPreserveHost(option.getPreserveHost());

        return api;
    }

    public static API portalAPI2API(PortalAPI portalAPI) {

        API api = new API();

        BeanUtils.copyProperties(portalAPI, api);
        api.setUriMatch(UriMatch.get(portalAPI.getUriMatch()));
        api.setProxyServices(portalAPI.getProxyServices().stream()
                                .map(ps -> portalService2Service(ps))
                                .collect(Collectors.toList()));
        api.setGateways(Arrays.asList(portalAPI.getGateway()));
        api.setName(portalAPI.getCode());
        return api;
    }

    public static Service portalService2Service(PortalService portalService) {

        Service s = new Service();
        s.setCode(portalService.getCode());
        s.setType(portalService.getType());
        s.setWeight(portalService.getWeight());
        s.setBackendService(portalService.getBackendService());
        return s;
    }

}
