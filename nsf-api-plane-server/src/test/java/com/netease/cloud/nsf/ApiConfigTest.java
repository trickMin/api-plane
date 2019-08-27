package com.netease.cloud.nsf;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.netease.cloud.nsf.meta.ApiOption;
import com.netease.cloud.nsf.meta.YxAPIModel;
import com.netease.cloud.nsf.service.GatewayService;

/**
 * Created by caukie on 2019/8/5.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiConfigTest {
    private static Logger logger = LoggerFactory.getLogger(ApiConfigTest.class);

    @Autowired
    private GatewayService gatewayService;

    @Test
    public void baseTest() throws Exception {
        System.out.println("功能测试环境");
        List<String> gateways = new ArrayList<>(3);
        gateways.add("demo-gateway");

        List<String> hosts = new ArrayList<>(3);
        gateways.add("api-demo.com");

        List<String> reqUris = new ArrayList<>(6);
        reqUris.add("/");
        reqUris.add("/status");

        List<String> methods = new ArrayList<>(3);
        methods.add("GET");

        List<String> proxyUris = new ArrayList<>();
        proxyUris.add("http://prxoy");

        ApiOption apiOption = new ApiOption();


        YxAPIModel apiModel = YxAPIModel.APIModelBuilder.anAPIModel()
                .withGateways(gateways)
                .withName("demo")
                .withHosts(hosts)
                .withRequestUris(reqUris)
                .withMethods(methods)
                .withProxyUris(proxyUris)
                .withService("demo-service")
                .withOption(apiOption)
                .build();

        gatewayService.updateAPI(apiModel);

        // gatewayService.deleteAPI();
    }

    /**
     * 测试方式：
     * 1，构造请求，下发到开发服务器，对网关进行配置；
     * 2，发起业务请求，检查配置是否正常；
     * 3，注意配置信息的reset；
     *
     * 用例主体对齐envoy：
     * https://g.hz.netease.com/qingzhou/envoy-function-instructions/blob/master/basic-function.md
     */
    /**
     *
     * 1，请求正常匹配到指定API：host+uri+method
     *  1）uri匹配支持：path（完全匹配），prefix（前缀匹配），regex（正则匹配）
     *  2) 配置创建，三者组合
     *  3）配置更新，覆盖
     *  4）配置删除（API级别）
     */

}
