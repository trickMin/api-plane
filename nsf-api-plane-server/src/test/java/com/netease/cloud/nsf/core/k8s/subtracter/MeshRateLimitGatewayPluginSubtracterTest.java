package com.netease.cloud.nsf.core.k8s.subtracter;


import com.netease.cloud.nsf.util.CommonUtil;
import me.snowdrop.istio.api.networking.v1alpha3.GatewayPlugin;
import org.junit.Assert;
import org.junit.Test;

public class MeshRateLimitGatewayPluginSubtracterTest {

    @Test
    public void testSubtract() {

        String yaml1 = "apiVersion: networking.istio.io/v1alpha3\n" +
                "kind: GatewayPlugin\n" +
                "metadata:\n" +
                "  name: a\n" +
                "  namespace: default\n" +
                "spec:\n" +
                "  gateway:\n" +
                "  - mesh\n" +
                "  plugins:\n" +
                "  - name: test\n" +
                "    settings:\n" +
                "      good: true\n" +
                "  - name: another\n" +
                "    settings:\n" +
                "      k: v    \n" +
                "  - name: envoy.ratelimit\n" +
                "    settings:\n" +
                "      rate_limits:\n" +
                "      - actions:\n" +
                "        - header_value_match:\n" +
                "            descriptor_value: Service[a]-User[none]-Gateway[null]-Api[null]-Id[a6628851-366c-4284-a737-078ae0ea0379]\n" +
                "            headers:\n" +
                "            - invert_match: false\n" +
                "              name: :authority\n" +
                "              regex_match: \"null\"\n" +
                "            - invert_match: true\n" +
                "              name: plugin1\n" +
                "              present_match: true\n" +
                "            - invert_match: false\n" +
                "              name: plugin2\n" +
                "              regex_match: ratelimit\n" +
                "        stage: 0\n" +
                "      - actions:\n" +
                "        - header_value_match:\n" +
                "            descriptor_value: Service[a]-User[none]-Gateway[null]-Api[null]-Id[96b8f174-b15c-4f3b-933a-30225ccaab4b]\n" +
                "            headers:\n" +
                "            - invert_match: false\n" +
                "              name: :authority\n" +
                "              regex_match: \"null\"\n" +
                "            - invert_match: true\n" +
                "              name: plugin1\n" +
                "              present_match: true\n" +
                "            - invert_match: false\n" +
                "              name: plugin2\n" +
                "              regex_match: ratelimit\n" +
                "        stage: 0\n" +
                "    \n";

        GatewayPlugin gp1 = CommonUtil.yaml2Obj(yaml1, GatewayPlugin.class);

        MeshRateLimitGatewayPluginSubtracter subtracter = new MeshRateLimitGatewayPluginSubtracter();
        GatewayPlugin subtracted = subtracter.subtract(gp1);

        Assert.assertEquals(2, subtracted.getSpec().getPlugins().size());
    }
}
