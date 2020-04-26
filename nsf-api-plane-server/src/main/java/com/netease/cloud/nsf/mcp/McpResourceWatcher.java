package com.netease.cloud.nsf.mcp;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/13
 **/
public interface McpResourceWatcher {
    void watch(Connection connection);

    void release(Connection connection);
}
