package com.netease.cloud.nsf.core.gateway;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.K8sResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.operator.IntegratedResourceOperator;
import com.netease.cloud.nsf.core.template.TemplateTranslator;
import com.netease.cloud.nsf.meta.API;
import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.util.K8sResourceEnum;
import com.netease.cloud.nsf.util.PathExpressionEnum;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.ExceptionConst;
import me.snowdrop.istio.api.IstioResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Executable;
import java.util.*;


/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/17
 **/
@Component
public class GatewayModelProcessor {

    private static final Logger logger = LoggerFactory.getLogger(GatewayModelProcessor.class);

    @Autowired
    IntegratedResourceOperator operator;

    @Autowired
    TemplateTranslator templateTranslator;

    @Autowired
    EditorContext editorContext;

    @Autowired
    IstioHttpClient istioHttpClient;

    private static final String baseApiResource = "gateway/baseApiResource";
    private static final String baseDestinationRule = "gateway/baseDestinationRule";

    /**
     * 将api转换为istio对应的规则
     *
     * @param api
     * @param namespace
     * @return
     */
    public List<IstioResource> translate(API api, String namespace) {

        List<IstioResource> resources = new ArrayList<>();
        List<String> rawResource = new ArrayList<>();
        List<String> gateways = api.getGateways();

        if (CollectionUtils.isEmpty(gateways)) throw new ApiPlaneException(ExceptionConst.GATEWAY_LIST_EMPTY);

        // TODO handle plugins first
        List<Endpoint> endpoints = istioHttpClient.getEndpointList();
        if (CollectionUtils.isEmpty(endpoints)) throw new ApiPlaneException(ExceptionConst.ENDPOINT_LIST_EMPTY);

        gateways.stream().forEach( gateway -> {
            Map<String, Object> apiResourceParams = createApiResourceParams(gateway, api, namespace, endpoints);
            String[] resourceStrs = templateTranslator.translate(baseApiResource, apiResourceParams, TemplateTranslator.DEFAULT_TEMPLATE_SPILIT);
            rawResource.addAll(Arrays.asList(resourceStrs));
        });

        api.getProxyUris().stream().forEach(proxyUri -> {
            Map<String, Object> destinationParams = createDestinationParams(proxyUri, api, namespace, gateways);
            String destinationRuleStr = templateTranslator.translate(baseDestinationRule, destinationParams);
            rawResource.add(destinationRuleStr);
        });

        rawResource.forEach(r -> logger.info(r));

        rawResource.stream()
                .forEach(r -> {
                    K8sResourceGenerator gen = K8sResourceGenerator.newInstance(r, ResourceType.YAML, editorContext);
                    K8sResourceEnum resourceEnum = K8sResourceEnum.get(gen.getKind());
                    IstioResource ir = (IstioResource) gen.object(resourceEnum.mappingType());
                    resources.add(ir);
                });

        return resources;
    }

    private Map<String, Object> createDestinationParams(String proxyUri, API api, String namespace, List<String> gateways) {

        Map<String, Object> params = new HashMap<>();
        String destinationRuleName = proxyUri.contains(".") ? proxyUri.substring(0, proxyUri.indexOf(".")) : proxyUri;
        params.put("destination_rule_name", destinationRuleName);
        params.put("host", proxyUri);
        params.put("namespace", namespace);
        params.put("api", api);
        params.put("gateway_instances", gateways);

        return params;
    }

    private Map<String, Object> createApiResourceParams(String gateway, API api, String namespace, List<Endpoint> endpoints) {

        String resourceName = String.format("%s-%s", api.getService(), gateway);

        Map<String, Object> params = new HashMap<>();
        List<Map<String, Object>> destinations = new ArrayList<>();
        params.put("api", api);
        params.put("resource_name", resourceName);
        params.put("namespace", namespace);
        params.put("gateway_instance", gateway);

        List<String> proxies = api.getProxyUris();
        for (int i = 0; i < proxies.size() ; i++) {
            for (Endpoint e : endpoints) {
                if (e.getHostname().equals(proxies.get(i))) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("port", e.getPort());
                    int weight = 100/proxies.size();
                    if (i == proxies.size() - 1) {
                        weight = 100 - 100*(proxies.size())/proxies.size();
                    }
                    param.put("weight", weight);
                    param.put("host", e.getHostname());
                    destinations.add(param);
                    break;
                }
            }
        }
        params.put("destinations", destinations);
        return params;
    }


    /**
     * 合并两个crd,新的和旧的重叠部分会用新的覆盖旧的
     * @param old
     * @param fresh
     * @return
     */
    public IstioResource merge(IstioResource old, IstioResource fresh) {

        if (fresh == null) return old;
        if (old == null) throw new ApiPlaneException(ExceptionConst.RESOURCE_NON_EXIST);
        return operator.merge(old, fresh);
    }

    /**
     * 在已有的istio crd中删去对应api部分
     *
     * @param old
     * @param api
     * @return
     */
    public IstioResource subtract(IstioResource old, String api) {
        K8sResourceEnum resource = K8sResourceEnum.get(old.getKind());
        switch (resource) {
            case VirtualService: {
                ResourceGenerator gen = ResourceGenerator.newInstance(old, ResourceType.OBJECT, editorContext);
                gen.removeElement(PathExpressionEnum.REMOVE_VS_HTTP.translate(api));
                return (IstioResource) gen.object(resource.mappingType());
            }
            case DestinationRule: {
                ResourceGenerator gen = ResourceGenerator.newInstance(old, ResourceType.OBJECT, editorContext);
                gen.removeElement(PathExpressionEnum.REMOVE_DST_SUBSET.translate(api));
                return (IstioResource) gen.object(resource.mappingType());
            }
            default:
                return old;
        }
    }
}
