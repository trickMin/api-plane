package com.netease.cloud.nsf.core.k8s.merger;


import com.netease.cloud.nsf.util.CommonUtil;
import me.snowdrop.istio.api.networking.v1alpha3.GatewayPlugin;
import me.snowdrop.istio.api.networking.v1alpha3.GatewayPluginBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class MeshRateLimitGatewayPluginMergerTest {


    @Test
    public void merge() {

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

        String yaml2 = "apiVersion: networking.istio.io/v1alpha3\n" +
                "kind: GatewayPlugin\n" +
                "metadata:\n" +
                "  name: a\n" +
                "  namespace: default\n" +
                "spec:\n" +
                "  gateway:\n" +
                "  - mesh\n" +
                "  plugins:\n" +
                "  - name: envoy.ratelimit\n" +
                "    settings:\n" +
                "      rate_limits:\n" +
                "      - actions:\n" +
                "        - header_value_match:\n" +
                "            descriptor_value: Service[c]-User[none]-Gateway[null]-Api[null]-Id[a6628851-366c-4284-a737-078ae0ea0379]\n" +
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
                "            descriptor_value: Service[c]-User[none]-Gateway[null]-Api[null]-Id[96b8f174-b15c-4f3b-933a-30225ccaab4b]\n" +
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
                "        stage: 0  ";

        String yaml3 = "apiVersion: networking.istio.io/v1alpha3\n" +
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
                "      k: v    \n";

        GatewayPlugin gp1 = CommonUtil.yaml2Obj(yaml1, GatewayPlugin.class);
        GatewayPlugin gp2 = CommonUtil.yaml2Obj(yaml2, GatewayPlugin.class);

        GatewayPlugin gp1_1 = CommonUtil.yaml2Obj(yaml1, GatewayPlugin.class);
        GatewayPlugin gp2_1 = CommonUtil.yaml2Obj(yaml2, GatewayPlugin.class);
        GatewayPlugin gp3 = CommonUtil.yaml2Obj(yaml3, GatewayPlugin.class);

        GatewayPlugin gp2_2 = CommonUtil.yaml2Obj(yaml2, GatewayPlugin.class);
        GatewayPlugin gp3_1 = CommonUtil.yaml2Obj(yaml3, GatewayPlugin.class);
        GatewayPlugin emptyGp = new GatewayPluginBuilder().build();

        MeshRateLimitGatewayPluginMerger merger = new MeshRateLimitGatewayPluginMerger();
        GatewayPlugin merged = merger.merge(gp1, gp2);

        Assert.assertEquals(extractRateLimitSettings(gp2), extractRateLimitSettings(merged));

        GatewayPlugin merged1 = merger.merge(gp1_1, emptyGp);
        Assert.assertEquals(2, merged1.getSpec().getPlugins().size());

        GatewayPlugin merged2 = merger.merge(gp3, gp2_1);
        Assert.assertEquals(extractRateLimitSettings(merged2), extractRateLimitSettings(gp2_1));

        GatewayPlugin merged3 = merger.merge(gp3_1, gp2_2);
        Assert.assertEquals(3, merged3.getSpec().getPlugins().size());
    }


    private Map<String, Object> extractRateLimitSettings(GatewayPlugin gp) {
        return gp.getSpec().getPlugins().stream()
                .filter(p -> p.getName().equals("envoy.ratelimit"))
                .findFirst()
                .get()
                .getSettings();
    }
}
