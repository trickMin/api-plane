package com.netease.cloud.nsf.util;

public interface Const {

    String PROXY_SERVICE_TYPE_STATIC = "STATIC";

    String PROXY_SERVICE_TYPE_DYNAMIC = "DYNAMIC";


    // 负载标签类型
    String LABEL_NSF_PROJECT_ID = "nsf-project";
    String LABEL_NSF_VERSION = "nsf-version";


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


    /**             TOPO              **/
    //TOPO NODE TYPE
    String NODE_TYPE_WORKLOAD = "workload";
    String NODE_TYPE_APP = "app";
    String NODE_TYPE_SERVICE = "service";
    String NODE_TYPE_UNKNOWN = "unknown";

    //TOPO GRAPH TYPE
    String GRAPH_TYPE_APP = "app";
    String GRAPH_TYPE_SERVICE = "service";
    String GRAPH_TYPE_VERSIONED_APP = "versionedApp";
    String GRAPH_TYPE_WORKLOAD = "workload";
    String GRAPH_TYPE_DEFAULT = GRAPH_TYPE_WORKLOAD;

    String UNKNOWN = "unknown";

    String PASS_THROUGH_CLUSTER = "PassthroughCluster";
    String BLACK_HOLE_CLUSTER = "BlackHoleCluster";


    //Metadata Key
    String META_KEY_DEST_SERVICES  = "destServices";
    String META_KEY_HAS_CB = "hasCB";
    String META_KEY_HAS_MISSING_SC = "hasMissingSC";
    String META_KEY_HAS_VS = "hasVS";
    String META_KEY_IS_DEAD = "isDead";
    String META_KEY_IS_EGRESS_CLUSTER = "isEgressCluster";
    String META_KEY_IS_Inaccessible = "isInaccessible";
    String META_KEY_IS_MISCONFIGURED = "isMisconfigured";
    String META_KEY_IS_MTLS = "isMTLS";
    String META_KEY_IS_OUTSIDE = "isOutside";
    String META_KEY_IS_ROOT = "isRoot";
    String META_KEY_IS_SERVICE_ENTRY = "isServiceEntry";
    String META_KEY_IS_UNUSED = "isUnused";
    String META_KEY_PROTOCOL_KEY = "protocolKey";
    String META_KEY_RESPONSE_TIME = "responseTime";

    String META_KEY_HTTP = "http";
    String META_KEY_HTTP_3XX = "http3xx";
    String META_KEY_HTTP_4XX = "http4xx";
    String META_KEY_HTTP_5XX = "http5xx";
    String META_KEY_HTTP_PERCENT_ERR = "httpPercentErr";
    String META_KEY_HTTP_PERCENT_REQ = "httpPercentReq";
    String META_KEY_HTTP_RESPONSES = "httpResponses";
    String META_KEY_HTTP_IN = "httpIn";
    String META_KEY_HTTP_IN_3XX = "httpIn3xx";
    String META_KEY_HTTP_IN_4XX = "httpIn4xx";
    String META_KEY_HTTP_IN_5XX = "httpIn5xx";
    String META_KEY_HTTP_OUT = "httpOut";

    /**             TOPO END            **/


}
