package com.netease.cloud.nsf.util.exception;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/25
 **/
public interface ExceptionConst {

    String ISTIO_POD_NON_EXIST = "Istio pod is non-exist";
    String RESOURCE_NON_EXIST = "Resource is non-exist";
    String RESOURCE_KIND_MISMATCH = "Resource kind is mismatch";
    String RESOURCES_DIFF_IDENTITY = "Resources have different identities";
    String GATEWAY_LIST_EMPTY = "gateway list is empty";
    String PROXY_URI_LIST_EMPTY = "proxy uri list is empty";


    String ENDPOINT_LIST_EMPTY = "endpoint list is empty";
    String JSON_TO_YAML_FAILED = "translate json to yaml failed";

}
