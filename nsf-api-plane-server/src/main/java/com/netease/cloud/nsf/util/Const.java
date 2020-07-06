package com.netease.cloud.nsf.util;

public interface Const {

    String PROXY_SERVICE_TYPE_STATIC = "STATIC";

    String PROXY_SERVICE_TYPE_DYNAMIC = "DYNAMIC";


    // 负载标签类型
//    String LABEL_NSF_PROJECT_ID = "nsf-project";
//    String LABEL_NSF_VERSION = "version";
    String LABEL_NSF_ENV = "nsf-env";
    String LABEL_NAMESPACE_INJECTION = "istio-injection";

    // 负载注解
    String ISTIO_INJECT_ANNOTATION = "sidecar.istio.io/inject";


    //http 方法
    String GET_METHOD = "GET";

    String POST_METHOD = "POST";

    String PUT_METHOD = "PUT";

    String HEAD_METHOD = "HEAD";

    String DELETE_METHOD = "DELETE";

    String OPTIONS_METHOD = "OPTIONS";

    // 默认请求头
    String INTERFACE_CALL_TYPE_INNER = "inner";
    String ACCEPT_LANGUAGE_ZH = "zh";

    String OPTION_TRUE = "true";
    String OPTION_FALSE = "false";
    String OPTION_ENABLED = "enabled";
    String OPTION_DISABLED = "disabled";


    String SERVICE_TYPE_CONSUL = "Consul";
    String SERVICE_TYPE_K8S = "Kubernetes";

    String DOWNLOAD_ENVOY_EVENT = "1";
    String DELETE_ENVOY_EVENT = "2";

    String DOWNLOAD_DAEMONSET_NAME = "nsf-api-plane-daemon";
    String DOWNLOAD_DAEMONSET_NAMESPACE = "istio-system";
    String DOWNLOAD_DAEMONSET_PORT = "9050";

    String SIDECAR_CONTAINER = "istio-proxy";
    String SIDECAR_CONTAINER_ERROR = "1";
    String SIDECAR_CONTAINER_SUCCESS = "0";




    int VERSION_MANAGER_CRD_EXIST = 1;
    int VERSION_MANAGER_CRD_MISSING = 2;
    int VERSION_MANAGER_CRD_DEFAULT = 3;

    // DestinationRule loadbalancer type

    String LB_TYPE_ROUND_ROBIN = "ROUND_ROBIN";
    String LB_TYPE_RANDOM = "RANDOM";
    String LB_TYPE_LEAST_CONN = "LEAST_CONN";
    String LB_TYPE_PASSTHROUGH = "PASSTHROUGH";
    String LB_TYPE_CONSISTENT_HASH = "CONSISTENT_HASH";

    String WORKLOAD_UPDATE_TIME_ANNOTATION = "nsf_workload_update_time";
    String WORKLOAD_OPERATION_TYPE_ANNOTATION = "nsf_workload_operation_type";
    String WORKLOAD_OPERATION_TYPE_ANNOTATION_INJECT = "inject";
    String WORKLOAD_OPERATION_TYPE_ANNOTATION_EXIT = "exit";

    String CONTAINER_STATUS_RUNNING = "Running";
    String CONTAINER_STATUS_WAITING = "Waiting";
    String CONTAINER_STATUS_TERMINATED = "Terminated";

    String SERVICE_MESH_PLUGIN_NAME_CIRCUIT_BREAKER = "com.netease.circuitbreaker";
    String SERVICE_MESH_CIRCUIT_BREAKER_KIND = "circuit-breaker";
    String SERVICE_MESH_PLUGIN_NAME_DOWNGRADE = "com.netease.dynamicdowngrade";
    String SERVICE_MESH_DOWNGRADE_KIND = "dynamic-downgrade";

    String RESOURCE_TARGET = "project";

    String DEFAULT_SIDECAR = "envoy";
    String SIDECAR_VERSION_PATTERN = "sidecar_[\\d]+\\.[\\d]+\\.[\\d]+_[0-9a-zA-Z]{32}";

    String SEPARATOR_DOT = ".";

    String NSF_LABEL_KEY_CLUSTER = "com.netease.nsf.cluster";
}
