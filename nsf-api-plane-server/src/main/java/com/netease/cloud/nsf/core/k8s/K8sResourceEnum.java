package com.netease.cloud.nsf.core.k8s;


import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.slime.api.microservice.v1alpha1.SmartLimiterList;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.*;
import io.fabric8.kubernetes.client.utils.URLUtils;
import me.snowdrop.istio.api.authentication.v1alpha1.Policy;
import me.snowdrop.istio.api.authentication.v1alpha1.PolicyList;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import me.snowdrop.istio.api.rbac.v1alpha1.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/23
 **/
public enum K8sResourceEnum {
    VirtualService(VirtualService.class, VirtualServiceList.class, "/apis/networking.istio.io/v1alpha3/namespaces/%s/virtualservices"),
    DestinationRule(DestinationRule.class, DestinationRuleList.class, "/apis/networking.istio.io/v1alpha3/namespaces/%s/destinationrules"),
    ServiceRole(ServiceRole.class, ServiceRoleList.class, "/apis/rbac.istio.io/v1alpha1/namespaces/%s/serviceroles"),
    ServiceRoleBinding(ServiceRoleBinding.class, ServiceRoleBindingList.class, "/apis/rbac.istio.io/v1alpha1/namespaces/%s/servicerolebindings"),
    Policy(Policy.class, PolicyList.class, "/apis/authentication.istio.io/v1alpha1/namespaces/%s/policies"),
    ServiceAccount(ServiceAccount.class, ServiceAccountList.class, "/api/v1/namespaces/%s/serviceaccounts"),
    Gateway(Gateway.class, GatewayList.class, "/apis/networking.istio.io/v1alpha3/namespaces/%s/gateways"),
    Pod(Pod.class, PodList.class, "/api/v1/namespaces/%s/pods"),
    ClusterRbacConfig(RbacConfig.class, RbacConfigList.class, "/apis/rbac.istio.io/v1alpha1/clusterrbacconfigs"),
    RbacConfig(RbacConfig.class, RbacConfigList.class, "/apis/rbac.istio.io/v1alpha1/clusterrbacconfigs"),
    SharedConfig(SharedConfig.class, SharedConfigList.class, "/apis/networking.istio.io/v1alpha3/namespaces/%s/sharedconfigs"),
    ServiceEntry(ServiceEntry.class, ServiceEntryList.class, "/apis/networking.istio.io/v1alpha3/namespaces/%s/serviceentries"),
    PluginManager(PluginManager.class, PluginManagerList.class, "/apis/networking.istio.io/v1alpha3/namespaces/%s/pluginmanagers"),
    Deployment(Deployment.class, DeploymentList.class, "/apis/extensions/v1beta1/namespaces/%s/deployments"),
    Endpoints(Endpoints.class, EndpointsList.class, "/api/v1/namespaces/%s/endpoints/"),
    DaemonSet(DaemonSet.class, DaemonSetList.class, "/apis/extensions/v1beta1/namespaces/%s/daemonsets"),
    Service(Service.class, ServiceList.class, "/api/v1/namespaces/%s/services/"),
    StatefulSet(StatefulSet.class, StatefulSetList.class, "/apis/apps/v1/namespaces/%s/statefulsets/"),
    ReplicaSet(ReplicaSet.class, ReplicaSetList.class, "/apis/extensions/v1beta1/namespaces/%s/replicasets/"),
    VersionManager(VersionManager.class, VersionManagerList.class, "/apis/networking.istio.io/v1alpha3/namespaces/%s/versionmanagers"),
    NameSpace(Namespace.class, NamespaceList.class, "/api/v1/namespaces/%s"),
    GatewayPlugin(GatewayPlugin.class, GatewayPluginList.class, "/apis", "networking.istio.io/v1alpha3", "gatewayplugins", "clustered".equals(System.getProperty("gatewaypluginScope"))),
    MixerUrlPattern(MixerUrlPattern.class, MixerUrlPatternList.class, "/apis/networking.istio.io/v1alpha3/namespaces/%s/mixerurlpatterns"),
    ConfigMap(ConfigMap.class, ConfigMapList.class, "/api/v1/namespaces/%s/configmaps"),
    SmartLimiter(com.netease.slime.api.microservice.v1alpha1.SmartLimiter.class, SmartLimiterList.class, "/apis/microservice.netease.com/v1alpha1/namespaces/%s/smartlimiters"),
    ;

    private Class<? extends HasMetadata> mappingType;
    private Class<? extends KubernetesResourceList> mappingListType;
    private String selfLink;
    private Boolean isClustered;

    K8sResourceEnum(Class<? extends HasMetadata> mappingType, Class<? extends KubernetesResourceList> mappingListType, String selfLink) {
        this.mappingType = mappingType;
        this.mappingListType = mappingListType;
        this.selfLink = selfLink;
        this.isClustered = false;
    }

    K8sResourceEnum(Class<? extends HasMetadata> mappingType, Class<? extends KubernetesResourceList> mappingListType, String prefix, String apiVersion, String name, Boolean isClusteredScope) {
        this.mappingType = mappingType;
        this.mappingListType = mappingListType;
        this.isClustered = isClusteredScope;
        if (isClusteredScope) {
            this.selfLink = URLUtils.pathJoin(prefix, apiVersion, name);
        } else {
            this.selfLink = URLUtils.pathJoin(prefix, apiVersion, "namespaces/%s", name);
        }
    }

    public String selfLink() {
        return selfLink;
    }

    public String selfLink(String namespace) {
        return selfLink.contains("%s") ? String.format(selfLink, namespace) : selfLink;
    }

    public String selfLink(String masterUrl, String namespace) {
        return URLUtils.pathJoin(masterUrl, selfLink(namespace));
    }

    public Boolean isClustered() {
        return isClustered;
    }

    public Boolean isNamespaced() {
        return !isClustered;
    }

    public Class<? extends HasMetadata> mappingType() {
        return mappingType;
    }

    public Class<? extends KubernetesResourceList> mappingListType() {
        return mappingListType;
    }

    public static K8sResourceEnum getItem(String name) {
        Pattern pattern = Pattern.compile("(.*)List$");
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            return get(matcher.group(1));
        }
        return get(name);
    }

    public static K8sResourceEnum get(String name) {
        for (K8sResourceEnum k8sResourceEnum : values()) {
            if (k8sResourceEnum.name().equalsIgnoreCase(name)) {
                return k8sResourceEnum;
            }
        }
        throw new ApiPlaneException("Unsupported resource types: " + name);
    }
}
