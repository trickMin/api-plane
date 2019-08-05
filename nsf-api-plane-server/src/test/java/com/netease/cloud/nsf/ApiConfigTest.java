package com.netease.cloud.nsf;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by caukie on 2019/8/5.
 */
@RunWith(SpringRunner.class)
public class ApiConfigTest {
    @Test
    public void baseTest() {
        System.out.println("功能测试环境");
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
