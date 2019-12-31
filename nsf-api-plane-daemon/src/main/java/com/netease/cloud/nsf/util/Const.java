package com.netease.cloud.nsf.util;

public interface Const {

    String PROXY_SERVICE_TYPE_STATIC = "STATIC";

    String PROXY_SERVICE_TYPE_DYNAMIC = "DYNAMIC";


    // 负载标签类型
    String LABEL_NSF_PROJECT_ID = "nsf-project";
    String LABEL_NSF_VERSION = "version";
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

    public static final String DEFAULT_NOS_SECRET_KEY = "b0bf90a8bead4b71a0027590360136d2";
    public static final String DEFAULT_NOS_ACCESS_KEY = "304263b6badc4c6b8ead2830a84e48e1";
    public static final String DEFAULT_NOS_BUCKETNAME = "qingzhou-gitops";
    public static final String DEFAULT_NOS_NOSENDPOINT = "nos-eastchina1.126.net";
    public static final String DEFAULT_NOS_NOS_FILE_PATH = "nsf_envoy";

    String DOWNLOAD_ENVOY_EVENT = "1";
    String DELETE_ENVOY_EVENT = "2";



}
