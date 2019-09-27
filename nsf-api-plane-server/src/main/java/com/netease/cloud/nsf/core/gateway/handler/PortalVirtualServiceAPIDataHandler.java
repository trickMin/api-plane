package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.gateway.processor.ModelProcessor;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.API;
import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.meta.Service;
import com.netease.cloud.nsf.service.PluginService;
import com.netease.cloud.nsf.util.Const;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.ExceptionConst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.netease.cloud.nsf.core.template.TemplateConst.VIRTUAL_SERVICE_DESTINATIONS;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/27
 **/
public class PortalVirtualServiceAPIDataHandler extends BaseVirtualServiceAPIDataHandler {

    public PortalVirtualServiceAPIDataHandler(ModelProcessor subModelProcessor, PluginService pluginService, List<FragmentWrapper> fragments, List<Endpoint> endpoints) {
        super(subModelProcessor, pluginService, fragments, endpoints);
    }

    @Override
    String produceRoute(API api, List<Endpoint> endpoints, String subset) {

        List<Map<String, Object>> destinations = new ArrayList<>();

        //only one gateway
        List<String> gateways = api.getGateways();
        String gateway = gateways.get(0);

        for (Service service : api.getProxyServices()) {

            Map<String, Object> param = new HashMap<>();
            param.put("weight", service.getWeight());
            param.put("subset", service.getCode() + "-" + gateway);

            Integer port = -1;
            String host = decorateHost(service.getCode());

            if (Const.PROXY_SERVICE_TYPE_DYNAMIC.equals(service.getType())) {
                for (Endpoint e : endpoints) {
                    if (e.getHostname().equals(service.getBackendService())) {
                        port = e.getPort();
                        host = service.getBackendService();
                        break;
                    }
                }
            } else if (Const.PROXY_SERVICE_TYPE_STATIC.equals(service.getType())) {
                port = 80;
            }

            if (port == -1) throw new ApiPlaneException(String.format("%s:%s", ExceptionConst.TARGET_SERVICE_NON_EXIST, service.getBackendService()));

            param.put("port", port);
            param.put("host", host);
            destinations.add(param);
        }

        String destinationStr = subModelProcessor.process(apiVirtualServiceRoute, TemplateParams.instance().put(VIRTUAL_SERVICE_DESTINATIONS, destinations));
        return destinationStr;
    }
}
