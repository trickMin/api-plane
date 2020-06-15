package com.netease.cloud.nsf.mcp;

import com.google.common.collect.ImmutableList;
import com.netease.cloud.nsf.core.BaseTest;
import com.netease.cloud.nsf.mcp.dao.ResourceDao;
import com.netease.cloud.nsf.mcp.dao.meta.Resource;
import com.netease.cloud.nsf.mcp.ratelimit.RlsClusterClient;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.when;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/6/11
 **/
public class RlsClientTest extends BaseTest {

    @MockBean
    ResourceDao resourceDao;

    // 连接失败时，自动重试连接，并且不会影响后面的连接请求
    @Test
    public void test() {
        Resource mockResource = new Resource();
        mockResource.setConfig("{\"apiVersion\":\"v1\",\"kind\":\"ConfigMap\",\"metadata\":{\"labels\":{\"skiff-api-plane-version\":\"release-1.2\",\"skiff-api-plane-type\":\"api-plane\"},\"name\":\"rate-limit-config\",\"namespace\":\"gateway-system\"},\"data\":{\"config.yaml\":\"descriptors: []\\ndomain: qingzhou\\n\"}}");
        mockResource.setLabel("{[skiff-api-plane-type,api-plane][skiff-api-plane-version,release-1.2]}");
        mockResource.setCollection("api/v1/configmaps");
        mockResource.setName("gateway-system/rate-limit-config");
        when(resourceDao.list(McpResourceEnum.ConfigMap.getCollection())).thenReturn(ImmutableList.of(mockResource));

        McpOptions options = new McpOptions();
        options.registerRls("127.0.0.1:80");
        options.registerRls("127.0.0.2:81");

        McpMarshaller marshaller = new McpMarshaller(options);
        RlsClusterClient clusterClient = new RlsClusterClient(options, resourceDao, marshaller);
        clusterClient.sync();
    }
}
