package com.netease.cloud.nsf.core.template;

/**
 * 支持TemplateWrapper的regex expression
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/2
 **/
public interface TemplateConst {
    String DESCRIPTION_TAG = "(?m)^#(?!@)(.*)$";
    String LABEL_TAG = "(?m)^#@(.*)=(.*)$";
    String BLANK_LINE = "(?m)^\\s*$(?:\\n|\\r\\n)";
    String IGNORE_SCHEME = "(?m)^(?!#)(.*)$";


    /** ---------- 模板占位符名 begin ---------- **/

    /** 公用 **/

    String NAMESPACE = "t_namespace";
    String API_GATEWAY = "t_api_gateway";
    String API = "t_api";
    String API_SERVICE = "t_api_service";
    String API_NAME = "t_api_name";
    String API_LOADBALANCER = "t_api_loadBalancer";
    String API_CONNECT_TIMEOUT = "t_api_connect_timeout";
    String API_IDLE_TIMEOUT = "t_api_idle_timeout";
    String API_RETRIES = "t_api_retries";
    String API_PRESERVE_HOST = "t_api_preserve_host";
    String API_PRIORITY = "t_api_priority";


    /** VirtualService **/

    String VIRTUAL_SERVICE_NAME = "t_virtual_service_name";
    String VIRTUAL_SERVICE_SUBSET_NAME = "t_virtual_service_subset_name";
    String VIRTUAL_SERVICE_DESTINATIONS = "t_virtual_service_destinations";
    String VIRTUAL_SERVICE_MATCH_YAML = "t_virtual_service_match_yaml";
    String VIRTUAL_SERVICE_ROUTE_YAML = "t_virtual_service_route_yaml";
    String VIRTUAL_SERVICE_EXTRA_YAML = "t_virtual_service_extra_yaml";
    String VIRTUAL_SERVICE_HOSTS_YAML = "t_virtual_service_hosts_yaml";
    String VIRTUAL_SERVICE_API_YAML = "t_virtual_service_api_yaml";
    String VIRTUAL_SERVICE_HOSTS = "t_virtual_service_hosts";

    /**
     * MATCH级別插件
     */
    String API_MATCH_PLUGINS = "t_api_match_plugins";

    /**
     * API级别插件
     */
    String API_API_PLUGINS = "t_api_api_plugins";

    /**
     * HOST级别插件
     */
    String API_HOST_PLUGINS = "t_api_host_plugins";

    /**
     * api请求uri
     */
    String API_REQUEST_URIS = "t_api_request_uris";
    /**
     * api请求方法
     */
    String API_METHODS = "t_api_methods";

    /**
     * api请求header
     */
    String API_HEADERS = "t_api_headers";

    /**
     * api请求query params
     */
    String API_QUERY_PARAMS = "t_api_query_params";


    /** DestinationRule **/

    String DESTINATION_RULE_NAME = "t_destination_rule_name";
    String DESTINATION_RULE_HOST = "t_destination_rule_host";

    String API_GATEWAYS = "t_api_gateways";


    /** ServiceEntry **/
    String SERVICE_ENTRY_NAME = "t_service_entry_name";
    String SERVICE_ENTRY_HOST = "t_service_entry_host";
    String SERVICE_ENTRY_PROTOCOL = "t_service_entry_protocol";
    String SERVICE_ENTRY_PROTOCOL_NAME = "t_service_entry_protocol_name";
    String SERVICE_ENTRY_PROTOCOL_PORT = "t_service_entry_protocol_port";

    /** Gateway **/
    String GATEWAY_NAME = "t_gateway_name";
    String GATEWAY_HOSTS = "t_gateway_hosts";


    /** SharedConfig **/
    String SHARED_CONFIG_DESCRIPTOR = "t_shared_config_descriptor";

    /** PluginManager **/
    String PLUGIN_MANAGER_NAME = "t_plugin_manager_name";
    String PLUGIN_MANAGER_WORKLOAD_LABELS = "t_plugin_manager_workload_labels";
    String PLUGIN_MANAGER_PLUGINS = "t_plugin_manager_plugins";

    /** ---------- 模板占位符名 end ---------- **/

}
