package com.netease.cloud.nsf.core.servicemesh;

import com.netease.cloud.nsf.core.BaseTest;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.meta.ServiceMeshRateLimit;
import com.netease.cloud.nsf.mock.MockK8sConfigStore;
import com.netease.cloud.nsf.service.PluginService;
import me.snowdrop.istio.api.networking.v1alpha3.Sidecar;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

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
        meshConfigManager = new K8sServiceMeshConfigManager(modelEngine, k8sConfigStore, pluginService);
    }

    @Test
    public void testUpdateRateLimit() {

        //单机
        ServiceMeshRateLimit rateLimit =
                buildMeshRateLimit("z.default",
                        "default",
                        "{\"kind\":\"mesh-rate-limiting\",\"limit_by_list\":[{\"pre_condition\":[{\"custom_extractor\":\"Header[plugin1]\",\"operator\":\"present\",\"invert\":true},{\"custom_extractor\":\"Header[plugin2]\",\"operator\":\"=\",\"right_value\":\"ratelimit\"}],\"hour\":2,\"second\":2,\"type\":\"local\"},{\"pre_condition\":[{\"custom_extractor\":\"Header[plugin1]\",\"operator\":\"present\",\"invert\":true},{\"custom_extractor\":\"Header[plugin2]\",\"operator\":\"=\",\"right_value\":\"ratelimit\"}],\"hour\":2,\"second\":2,\"type\":\"local\"}]}");

        meshConfigManager.updateRateLimit(rateLimit);
        Assert.assertEquals(3, k8sConfigStore.size());
        meshConfigManager.deleteRateLimit(rateLimit);
        Assert.assertEquals(1, k8sConfigStore.size());

        //TODO 全局插件



        //TODO 全局+单机混用
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
