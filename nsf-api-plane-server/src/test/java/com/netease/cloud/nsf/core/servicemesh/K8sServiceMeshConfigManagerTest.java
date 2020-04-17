package com.netease.cloud.nsf.core.servicemesh;

import com.netease.cloud.nsf.core.BaseTest;
import com.netease.cloud.nsf.meta.ServiceMeshRateLimit;
import com.netease.cloud.nsf.mock.MockK8sConfigStore;
import com.netease.cloud.nsf.service.PluginService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"globalPluginConfigEnv = mesh"})
public class K8sServiceMeshConfigManagerTest extends BaseTest {

    @Autowired
    ServiceMeshIstioModelEngine modelEngine;

    @Autowired
    PluginService pluginService;

    K8sServiceMeshConfigManager meshConfigManager;

    MockK8sConfigStore k8sConfigStore;

    @Before
    public void init() {
        k8sConfigStore = new MockK8sConfigStore();
        meshConfigManager = new K8sServiceMeshConfigManager(modelEngine, k8sConfigStore, pluginService);
    }

    @Test
    public void updateRateLimit() {

        ServiceMeshRateLimit rateLimit =
                buildMeshRateLimit("z.default",
                        "default",
                        "{\"kind\":\"mesh-rate-limiting\",\"limit_by_list\":[{\"pre_condition\":[{\"custom_extractor\":\"Header[plugin1]\",\"operator\":\"present\",\"invert\":true},{\"custom_extractor\":\"Header[plugin2]\",\"operator\":\"=\",\"right_value\":\"ratelimit\"}],\"hour\":2,\"second\":2,\"type\":\"Local\"},{\"pre_condition\":[{\"custom_extractor\":\"Header[plugin1]\",\"operator\":\"present\",\"invert\":true},{\"custom_extractor\":\"Header[plugin2]\",\"operator\":\"=\",\"right_value\":\"ratelimit\"}],\"hour\":2,\"second\":2,\"type\":\"Local\"}]}");

        meshConfigManager.updateRateLimit(rateLimit);
        Assert.assertEquals(2, k8sConfigStore.size());
        meshConfigManager.deleteRateLimit(rateLimit);
        Assert.assertEquals(0, k8sConfigStore.size());
    }

    private ServiceMeshRateLimit buildMeshRateLimit(String host, String namespace, String plugin) {
        ServiceMeshRateLimit rateLimit = new ServiceMeshRateLimit();
        rateLimit.setHost(host);
        rateLimit.setNamespace(namespace);
        rateLimit.setPlugin(plugin);
        return rateLimit;
    }

}
