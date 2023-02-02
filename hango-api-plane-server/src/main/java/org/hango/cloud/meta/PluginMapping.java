package org.hango.cloud.meta;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/11/21
 */
public enum PluginMapping {

    REWRITE("rewrite","proxy.filters.http.path_rewrite","RewriteProcessor"),
    JSONP("jsonp","proxy.filters.http.jsonpfilter","JsonpProcessor"),
    IANUS_REQUEST_TRANSFORMER("ianus-request-transformer","proxy.filters.http.transformation","TransformProcessor"),
    TRANSFORMER("transformer","proxy.filters.http.transformation","TransformProcessor"),
    STATIC_DOWNGRADE("static-downgrade","proxy.filters.http.staticdowngrade","StaticDowngradeProcessor"),
    DYNAMIC_DOWNGRADE("dynamic-downgrade","proxy.filters.http.dynamicdowngrade","DynamicDowngradeProcessor"),
    LOCAL_LIMITING("local-limiting","","SmartLimiterProcessor"),
    RATE_LIMITING("rate-limiting","","SmartLimiterProcessor"),
    IANUS_PERCENT_LIMIT("ianus-percent-limit","envoy.filters.http.fault","FlowLimitProcessor"),
    IP_RESTRICTION("ip-restriction","proxy.filters.http.iprestriction","IpRestrictionProcessor"),
    UA_RESTRICTION("ua-restriction","proxy.filters.http.ua_restriction","UaRestrictionProcessor"),
    REFERER_RESTRICTION("referer-restriction","proxy.filters.http.referer_restriction","RefererRestrictionProcessor"),
    HEADER_RESTRICTION("header-restriction","proxy.filters.http.header_restriction","HeaderRestrictionProcessor"),
    TRAFFIC_MARK("traffic-mark","proxy.filters.http.header_rewrite","TrafficMarkProcessor"),
    RESPONSE_HEADER_REWRITE("response-header-rewrite","proxy.filters.http.header_rewrite","ResponseHeaderRewriteProcessor"),
    CORS("cors","envoy.filters.http.cors","CorsProcessor"),
    CACHE("cache","proxy.filters.http.super_cache","Cache"),
    LOCAL_CACHE("local-cache","proxy.filters.http.local_cache","LocalCache"),
    REDIS_CACHE("redis-cache","proxy.filters.http.redis_cache","RedisCache"),
    // 兼容21.0.x版本认证插件，22.0.x版本认证插件已拆分为sign-auth、jwt-auth和oauth2-auth
    SUPER_AUTH("super-auth","proxy.filters.http.super_authz","PreviousVersionSuperAuth"),
    SIGN_AUTH("sign-auth","proxy.filters.http.super_authz","SuperAuth"),
    OAUTH2_AUTH("oauth2-auth","proxy.filters.http.super_authz","SuperAuth"),
    JWT_AUTH("jwt-auth","envoy.filters.http.jwt_authn","JwtAuth"),
    BASIC_RBAC("basic-rbac","envoy.filters.http.rbac","BasicRbac"),
    REQUEST_TRANSFORMER("request-transformer","proxy.filters.http.transformation","DefaultProcessor"),
    CIRCUIT_BREAKER("circuit-breaker","proxy.filters.http.circuitbreaker","CircuitBreakerProcessor"),
    FUNCTION("function","envoy.filters.http.lua","FunctionProcessor"),
    SOAP_JSON_TRANSCODER("soap-json-transcoder","proxy.filters.http.soapjsontranscoder","SoapJsonTranscoderProcessor"),
    IANUS_ROUTER("ianus-router","envoy.filters.http.fault","RouteProcessor"),
    WAF("waf","proxy.filters.http.waf","WafProcessor"),
    TRACE("trace","proxy.filters.http.rider","RestyProcessor"),

    //默认处理
    RESTY("resty","proxy.filters.http.rider","RestyProcessor"),
    ;

    /**
     * 插件映射名称
     */
    private String mappingName;

    /**
     * 插件名称
     */
    private String name;

    /**
     * 插件处理类名
     */
    private String processorClass;

    PluginMapping(String mappingName, String name, String processorClass) {
        this.mappingName = mappingName;
        this.name = name;
        this.processorClass = processorClass;
    }

    public String getMappingName() {
        return mappingName;
    }

    public String getName() {
        return name;
    }

    public String getProcessorClass() {
        return processorClass;
    }

    public static PluginMapping getBymappingName(String mappingName){
        for (PluginMapping value : values()) {
            if (value.getMappingName().equals(mappingName)) {
                return value;
            }
        }
        return PluginMapping.RESTY;
    }
}
