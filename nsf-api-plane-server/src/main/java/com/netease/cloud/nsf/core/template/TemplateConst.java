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

    /**
     * 公用
     **/

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


    /**
     * VirtualService
     **/

    String VIRTUAL_SERVICE_NAME = "t_virtual_service_name";
    String VIRTUAL_SERVICE_SUBSET_NAME = "t_virtual_service_subset_name";
    String VIRTUAL_SERVICE_DESTINATIONS = "t_virtual_service_destinations";
    String VIRTUAL_SERVICE_MATCH_YAML = "t_virtual_service_match_yaml";
    String VIRTUAL_SERVICE_ROUTE_YAML = "t_virtual_service_route_yaml";
    String VIRTUAL_SERVICE_EXTRA_YAML = "t_virtual_service_extra_yaml";
    String VIRTUAL_SERVICE_HOSTS_YAML = "t_virtual_service_hosts_yaml";
    String VIRTUAL_SERVICE_API_YAML = "t_virtual_service_api_yaml";
    String VIRTUAL_SERVICE_HOSTS = "t_virtual_service_hosts";
    String VIRTUAL_SERVICE_HOST_LIST = "t_virtual_service_host_list";
    String VIRTUAL_SERVICE_HOST_HEADERS = "t_virtual_service_host_headers";
    String VIRTUAL_SERVICE_MATCH_PRIORITY = "t_virtual_service_match_priority";
    String VIRTUAL_SERVICE_PLUGIN_MATCH_PRIORITY = "t_virtual_service_plugin_match_priority";
    String VIRTUAL_SERVICE_SERVICE_TAG = "t_virtual_service_service_tag";
    String VIRTUAL_SERVICE_API_ID = "t_virtual_service_api_id";
    String VIRTUAL_SERVICE_API_NAME = "t_virtual_service_api_name";

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


    /**
     * DestinationRule
     **/

    String DESTINATION_RULE_NAME = "t_destination_rule_name";
    String DESTINATION_RULE_HOST = "t_destination_rule_host";
    String DESTINATION_RULE_CONSECUTIVE_ERRORS = "t_destination_rule_consecutive_errors";
    String DESTINATION_RULE_BASE_EJECTION_TIME = "t_destination_rule_base_ejection_time";
    String DESTINATION_RULE_MAX_EJECTION_PERCENT = "t_destination_rule_max_ejection_percent";
    String DESTINATION_RULE_PATH = "t_destination_rule_path";
    String DESTINATION_RULE_TIMEOUT = "t_destination_rule_timeout";
    String DESTINATION_RULE_EXPECTED_STATUSES = "t_destination_rule_expected_statuses";
    String DESTINATION_RULE_HEALTHY_INTERVAL = "t_destination_rule_healthy_interval";
    String DESTINATION_RULE_HEALTHY_THRESHOLD = "t_destination_rule_healthy_threshold";
    String DESTINATION_RULE_UNHEALTHY_INTERVAL = "t_destination_rule_unhealthy_interval";
    String DESTINATION_RULE_UNHEALTHY_THRESHOLD = "t_destination_rule_unhealthy_threshold";
    String DESTINATION_RULE_ALT_STAT_NAME = "t_destination_rule_alt_stat_name";
    String DESTINATION_RULE_LOAD_BALANCER = "t_destination_rule_load_balancer";
    String DESTINATION_RULE_EXTRA_SUBSETS = "t_destination_rule_extra_subsets";

    String API_GATEWAYS = "t_api_gateways";



    /**
     * ServiceEntry
     **/
    String SERVICE_ENTRY_NAME = "t_service_entry_name";
    String SERVICE_ENTRY_HOST = "t_service_entry_host";
    String SERVICE_ENTRY_PROTOCOL = "t_service_entry_protocol";
    String SERVICE_ENTRY_PROTOCOL_NAME = "t_service_entry_protocol_name";
    String SERVICE_ENTRY_PROTOCOL_PORT = "t_service_entry_protocol_port";

    /**
     * Gateway
     **/
    String GATEWAY_NAME = "t_gateway_name";
    String GATEWAY_HOSTS = "t_gateway_hosts";
    String GATEWAY_HTTP_10 = "t_gateway_http_10";


    /**
     * SharedConfig
     **/
    String SHARED_CONFIG_DESCRIPTOR = "t_shared_config_descriptor";

    /**
     * PluginManager
     **/
    String PLUGIN_MANAGER_NAME = "t_plugin_manager_name";
    String PLUGIN_MANAGER_WORKLOAD_LABELS = "t_plugin_manager_workload_labels";
    String PLUGIN_MANAGER_PLUGINS = "t_plugin_manager_plugins";

    /**
     * VersionManager
     **/
    String VERSION_MANAGER_WORKLOADS = "t_version_manager_workloads";

    /**
     * GatewayPlugin
     */
    String GATEWAY_PLUGIN_NAME = "t_gateway_plugin_name";
    String GATEWAY_PLUGIN_GATEWAYS = "t_gateway_plugin_gateways";
    String GATEWAY_PLUGIN_HOSTS = "t_gateway_plugin_hosts";
    String GATEWAY_PLUGIN_PLUGINS = "t_gateway_plugin_plugins";



    /** ---------- 模板占位符名 end ---------- **/

}
