package com.netease.cloud.nsf.core.servicemesh;

import com.netease.cloud.nsf.core.BaseTest;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.meta.ServiceMeshRateLimit;
import com.netease.cloud.nsf.mock.MockEventPublisher;
import com.netease.cloud.nsf.mock.MockK8sConfigStore;
import com.netease.cloud.nsf.service.PluginService;
import io.fabric8.kubernetes.api.model.HasMetadata;
import me.snowdrop.istio.api.networking.v1alpha3.Sidecar;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

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
        meshConfigManager = new K8sServiceMeshConfigManager(modelEngine, k8sConfigStore, pluginService, new MockEventPublisher());
        meshConfigManager.setRlsApp("rate-limit");
    }

    @Test
    public void testUpdateRateLimit() {

        boolean hasConfigMap = false;
        boolean hasGatewayPlugin = false;
        boolean hasSmartLimiter = false;

        //单机 smartlimiter + gatewayplugin
        ServiceMeshRateLimit rateLimit =
                buildMeshRateLimit("z.default",
                        "default",
                        "{\"kind\":\"mesh-rate-limiting\",\"limit_by_list\":[{\"pre_condition\":[{\"custom_extractor\":\"Header[plugin1]\",\"operator\":\"present\",\"invert\":true},{\"custom_extractor\":\"Header[plugin2]\",\"operator\":\"=\",\"right_value\":\"ratelimit\"}],\"hour\":2,\"second\":2,\"type\":\"local\"},{\"pre_condition\":[{\"custom_extractor\":\"Header[plugin1]\",\"operator\":\"present\",\"invert\":true},{\"custom_extractor\":\"Header[plugin2]\",\"operator\":\"=\",\"right_value\":\"ratelimit\"}],\"hour\":2,\"second\":2,\"type\":\"local\"}]}");

        meshConfigManager.updateRateLimit(rateLimit);
        Assert.assertEquals(2, k8sConfigStore.size());

        for (Map.Entry<MockK8sConfigStore.ResourceId, HasMetadata> entry : k8sConfigStore.map().entrySet()) {
            String kind = entry.getValue().getKind();
            if (kind.equals(K8sResourceEnum.SmartLimiter.name())) hasSmartLimiter = true;
            if (kind.equals(K8sResourceEnum.GatewayPlugin.name())) hasGatewayPlugin = true;
        }
        Assert.assertTrue(hasSmartLimiter && hasGatewayPlugin);
        hasSmartLimiter = false;
        hasGatewayPlugin = false;

        meshConfigManager.deleteRateLimit(rateLimit);
        Assert.assertEquals(0, k8sConfigStore.size());

        // 全局插件 configmap + gatewayplugin
        ServiceMeshRateLimit rateLimit1 =
                buildMeshRateLimit("z.default",
                        "default",
                        "{\"kind\":\"mesh-rate-limiting\",\"limit_by_list\":[{\"pre_condition\":[{\"custom_extractor\":\"Header[plugin1]\",\"operator\":\"present\",\"invert\":true},{\"custom_extractor\":\"Header[plugin2]\",\"operator\":\"=\",\"right_value\":\"ratelimit\"}],\"hour\":2,\"second\":2,\"type\":\"global\"},{\"pre_condition\":[{\"custom_extractor\":\"Header[plugin1]\",\"operator\":\"present\",\"invert\":true},{\"custom_extractor\":\"Header[plugin2]\",\"operator\":\"=\",\"right_value\":\"ratelimit\"}],\"hour\":2,\"second\":2,\"type\":\"global\"}]}");

        meshConfigManager.updateRateLimit(rateLimit1);
        Assert.assertEquals(2, k8sConfigStore.size());

        for (Map.Entry<MockK8sConfigStore.ResourceId, HasMetadata> entry : k8sConfigStore.map().entrySet()) {
            String kind = entry.getValue().getKind();
            if (kind.equals(K8sResourceEnum.ConfigMap.name())) hasConfigMap = true;
            if (kind.equals(K8sResourceEnum.GatewayPlugin.name())) hasGatewayPlugin = true;
        }
        Assert.assertTrue(hasConfigMap && hasGatewayPlugin);
        hasConfigMap = false;
        hasGatewayPlugin = false;

        //不清理configmap
        meshConfigManager.deleteRateLimit(rateLimit);
        Assert.assertEquals(1, k8sConfigStore.size());


        // 全局+单机混用
        ServiceMeshRateLimit rateLimit2 =
                buildMeshRateLimit("z.default",
                        "default",
                        "{\"kind\":\"mesh-rate-limiting\",\"limit_by_list\":[{\"pre_condition\":[{\"custom_extractor\":\"Header[plugin1]\",\"operator\":\"present\",\"invert\":true},{\"custom_extractor\":\"Header[plugin2]\",\"operator\":\"=\",\"right_value\":\"ratelimit\"}],\"hour\":2,\"second\":2,\"type\":\"global\"},{\"pre_condition\":[{\"custom_extractor\":\"Header[plugin1]\",\"operator\":\"present\",\"invert\":true},{\"custom_extractor\":\"Header[plugin2]\",\"operator\":\"=\",\"right_value\":\"ratelimit\"}],\"hour\":2,\"second\":2,\"type\":\"local\"}]}");

        meshConfigManager.updateRateLimit(rateLimit2);
        Assert.assertEquals(3, k8sConfigStore.size());

        for (Map.Entry<MockK8sConfigStore.ResourceId, HasMetadata> entry : k8sConfigStore.map().entrySet()) {
            String kind = entry.getValue().getKind();
            if (kind.equals(K8sResourceEnum.ConfigMap.name())) hasConfigMap = true;
            if (kind.equals(K8sResourceEnum.GatewayPlugin.name())) hasGatewayPlugin = true;
            if (kind.equals(K8sResourceEnum.SmartLimiter.name())) hasSmartLimiter = true;

        }
        Assert.assertTrue(hasConfigMap && hasGatewayPlugin && hasSmartLimiter);

        meshConfigManager.deleteRateLimit(rateLimit2);
        Assert.assertEquals(1, k8sConfigStore.size());
    }

    @Test
    public void testUpdateSidecarScope() throws InterruptedException {

        int threadCount = 16;
        //2个固定的hosts
        int fixedHosts = 2;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        String source = "a";
        String ns = "default";
        String target = "target";

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            TestSidecarJob job = new TestSidecarJob(
                    startLatch, endLatch, () -> meshConfigManager.updateSidecarScope(source, ns, target + index));
            new Thread(job).start();
        }
        //所有线程一起开始
        startLatch.countDown();
        //等待所有线程结束
        endLatch.await();

        Assert.assertEquals(1, k8sConfigStore.size());
        Sidecar sidecar = (Sidecar) k8sConfigStore.get(K8sResourceEnum.Sidecar.name(), ns, source);
        Assert.assertEquals(threadCount + fixedHosts, sidecar.getSpec().getEgress().get(0).getHosts().size());

    }

    class TestSidecarJob implements Runnable {

        CountDownLatch startLatch;
        CountDownLatch endLatch;
        Runnable job;
        public TestSidecarJob(CountDownLatch startLatch, CountDownLatch endLatch, Runnable job) {
            this.startLatch = startLatch;
            this.endLatch = endLatch;
            this.job = job;
        }

        @Override
        public void run() {
            try {
                startLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            job.run();
            endLatch.countDown();
        }
    }


    private ServiceMeshRateLimit buildMeshRateLimit(String host, String namespace, String plugin) {
        ServiceMeshRateLimit rateLimit = new ServiceMeshRateLimit();
        rateLimit.setHost(host);
        rateLimit.setNamespace(namespace);
        rateLimit.setPlugin(plugin);
        rateLimit.setServiceName(host.split("\\.")[0]);
        return rateLimit;
    }


}
