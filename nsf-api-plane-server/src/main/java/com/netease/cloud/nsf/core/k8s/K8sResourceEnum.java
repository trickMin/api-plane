package com.netease.cloud.nsf.core.k8s;


import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.proto.k8s.K8sTypes;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.slime.api.microservice.v1alpha1.SmartLimiterList;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.*;
import io.fabric8.kubernetes.client.utils.URLUtils;
import me.snowdrop.istio.api.authentication.v1alpha1.Policy;
import me.snowdrop.istio.api.authentication.v1alpha1.PolicyList;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import me.snowdrop.istio.api.rbac.v1alpha1.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/23
 **/

public enum K8sResourceEnum {
    VirtualService(
            K8sTypes.VirtualService.class,
            K8sTypes.VirtualServiceList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/networking.istio.io/v1alpha3/namespaces/%s/virtualservices")),
    DestinationRule(
            K8sTypes.DestinationRule.class,
            K8sTypes.DestinationRuleList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/networking.istio.io/v1alpha3/namespaces/%s/destinationrules")),
    ServiceRole(
            ServiceRole.class,
            ServiceRoleList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/rbac.istio.io/v1alpha1/namespaces/%s/serviceroles")),
    ServiceRoleBinding(
            ServiceRoleBinding.class,
            ServiceRoleBindingList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/rbac.istio.io/v1alpha1/namespaces/%s/servicerolebindings")),
    Policy(
            Policy.class,
            PolicyList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/authentication.istio.io/v1alpha1/namespaces/%s/policies")),
    ServiceAccount(
            ServiceAccount.class,
            ServiceAccountList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/api/v1/namespaces/%s/serviceaccounts")),
    Gateway(
            Gateway.class,
            GatewayList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/networking.istio.io/v1alpha3/namespaces/%s/gateways")),
    Pod(
            Pod.class,
            PodList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/api/v1/namespaces/%s/pods")),
    ClusterRbacConfig(
            RbacConfig.class,
            RbacConfigList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/rbac.istio.io/v1alpha1/clusterrbacconfigs")),
    RbacConfig(
            RbacConfig.class,
            RbacConfigList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/rbac.istio.io/v1alpha1/clusterrbacconfigs")),
    SharedConfig(
            SharedConfig.class,
            SharedConfigList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/networking.istio.io/v1alpha3/namespaces/%s/sharedconfigs")),
    ServiceEntry(
            ServiceEntry.class,
            ServiceEntryList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/networking.istio.io/v1alpha3/namespaces/%s/serviceentries")),
    PluginManager(
            K8sTypes.PluginManager.class,
            K8sTypes.PluginManagerList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/microservice.slime.io/v1alpha1/namespaces/%s/pluginmanagers")),
    Deployment(
            Deployment.class,
            DeploymentList.class,
            ImmutableMap.of(
                    K8sVersion.V1_11_0, "/apis/extensions/v1beta1/namespaces/%s/deployments",
                    K8sVersion.V1_17_0, "/apis/apps/v1/namespaces/%s/deployments"
            )),
    Endpoints(
            Endpoints.class,
            EndpointsList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/api/v1/namespaces/%s/endpoints/")),
    DaemonSet(
            DaemonSet.class,
            DaemonSetList.class,
            ImmutableMap.of(
                    K8sVersion.V1_11_0, "/apis/extensions/v1beta1/namespaces/%s/daemonsets",
                    K8sVersion.V1_17_0, "/apis/apps/v1/namespaces/%s/daemonsets"
            )),
    Service(
            Service.class,
            ServiceList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/api/v1/namespaces/%s/services/")),
    StatefulSet(
            StatefulSet.class,
            StatefulSetList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/apps/v1/namespaces/%s/statefulsets/")),
    ReplicaSet(
            ReplicaSet.class,
            ReplicaSetList.class,
            ImmutableMap.of(
                    K8sVersion.V1_11_0, "/apis/extensions/v1beta1/namespaces/%s/replicasets/",
                    K8sVersion.V1_17_0, "/apis/apps/v1/namespaces/%s/replicasets/"
            )),
    VersionManager(
            VersionManager.class,
            VersionManagerList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/networking.istio.io/v1alpha3/namespaces/%s/versionmanagers")),
    GlobalConfig(
            GlobalConfig.class,
            GlobalConfigList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/networking.istio.io/v1alpha3/globalconfigs")
    ),
    NameSpace(
            Namespace.class,
            NamespaceList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/api/v1/namespaces/%s")),
    //FIXME：传媒版本是否还遗留GlobalScope的GatewayPlugin
    EnvoyPlugin(
            K8sTypes.EnvoyPlugin.class,
            K8sTypes.EnvoyPluginList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/microservice.slime.io/v1alpha1/namespaces/%s/envoyplugins")),
    MixerUrlPattern(
            MixerUrlPattern.class,
            MixerUrlPatternList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/networking.istio.io/v1alpha3/namespaces/%s/mixerurlpatterns")),
    ConfigMap(
            ConfigMap.class,
            ConfigMapList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/api/v1/namespaces/%s/configmaps")),
    SmartLimiter(
            com.netease.slime.api.microservice.v1alpha1.SmartLimiter.class,
            SmartLimiterList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/microservice.netease.com/v1alpha1/namespaces/%s/smartlimiters")),
    Sidecar(
            me.snowdrop.istio.api.networking.v1alpha3.Sidecar.class,
            SidecarList.class,
            ImmutableMap.of(K8sVersion.V1_11_0, "/apis/networking.istio.io/v1alpha3/namespaces/%s/sidecars")),
    ;

    private Class<? extends HasMetadata> mappingType;
    private Class<? extends KubernetesResourceList> mappingListType;
    private String selfLink;
    private Boolean isClustered;

    K8sResourceEnum(Class<? extends HasMetadata> mappingType, Class<? extends KubernetesResourceList> mappingListType, Map<K8sVersion, String> selfLinkMap) {
        // 选择接近当前k8s版本但小于当前版本的selfLink
        K8sVersion currentVersion;
        String currentK8sVersion = System.getProperty("k8sVersion");
        if (StringUtils.isNotEmpty(currentK8sVersion)) {
            currentVersion = new K8sVersion(currentK8sVersion);
        } else {
            currentVersion = K8sVersion.V1_11_0;
        }
        K8sVersion closedVersion = select(selfLinkMap.keySet(), currentVersion);

        this.mappingType = mappingType;
        this.mappingListType = mappingListType;
        this.selfLink = selfLinkMap.get(closedVersion);
        this.isClustered = false;
    }

    public String selfLink() {
        return selfLink;
    }

    public String selfLink(String namespace) {
        //FIXME 后续可以使用优雅些的方式
        if (StringUtils.isEmpty(namespace)) {
            return selfLink.replace("namespaces/%s/", "");
        }
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

    /**
     * 查找最接近当前k8s版本但小于当前k8s版本的 k8sVersion
     * 例如：
     * 1. k8sVersions[v1.1.0, v1.7.0], currentVersion:v1.1.0，返回k8sVersion:v1.1.0
     * 2. k8sVersions[v1.5.0, v1.7.0], currentVersion:v1.1.0，找不到合适版本，报错
     * 3. k8sVersions[v1.5.0, v1.7.0], currentVersion:v1.6.0，返回k8sVersion:v1.5.0
     * 4. k8sVersions[v1.5.0, v1.7.0], currentVersion:v1.8.0，返回k8sVersion:v1.7.0
     *
     * @param k8sVersions
     * @param currentVersion 当前k8sVersion
     * @return
     */
    private K8sVersion select(Collection<K8sVersion> k8sVersions, K8sVersion currentVersion) {
        K8sVersion[] sortedVersion = k8sVersions.toArray(new K8sVersion[0]);
        Arrays.sort(sortedVersion);
        int length = sortedVersion.length;
        for (int i = 0; i < length; i++) {
            int compare = sortedVersion[i].compareTo(currentVersion);
            if (compare < 0) {
                if (i + 1 == length) {
                    return sortedVersion[i];
                }
            } else if (compare > 0) {
                if (i == 0) {
                    throw new RuntimeException(String.format("crd:%s are not compatible with the current K8S version: %s", this.name(), currentVersion));
                } else {
                    return sortedVersion[i - 1];
                }
            } else {
                return sortedVersion[i];
            }
        }
        throw new RuntimeException(String.format("crd:%s are not compatible with the current K8S version: %s", this.name(), currentVersion));
    }
}
