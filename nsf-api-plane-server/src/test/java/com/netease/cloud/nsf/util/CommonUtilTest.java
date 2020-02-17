package com.netease.cloud.nsf.util;

import com.netease.cloud.nsf.meta.Service;
import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/26
 **/
public class CommonUtilTest {

    @Test
    public void testValidIpPort() {

        String ip1 = "10.10.10.10:2131";
        String ip2 = "256.256.256.213:A";
        String ip3 = "10.10.10.10:65536";
        String ip4 = "10.10.10.10";

        Assert.assertTrue(CommonUtil.isValidIPPortAddr(ip1));
        Assert.assertTrue(!CommonUtil.isValidIPPortAddr(ip2));
        Assert.assertTrue(!CommonUtil.isValidIPPortAddr(ip3));
        Assert.assertTrue(!CommonUtil.isValidIPPortAddr(ip4));
    }

    @Test
    public void testHost2Regex() {

        String h1 = "*.163.com";
        String h2 = "www.*.com";

        String add1 = "a.163.com";
        String add2 = ".163.com";
        String add3 = "www.163.com";
        String add4 = "www.com";

        Pattern p1 = Pattern.compile(CommonUtil.host2Regex(h1));
        Pattern p2 = Pattern.compile(CommonUtil.host2Regex(h2));

        Assert.assertTrue(p1.matcher(add1).find());
        Assert.assertTrue(!p1.matcher(add2).find());
        Assert.assertTrue(p1.matcher(add3).find());
        Assert.assertTrue(p2.matcher(add3).find());
        Assert.assertTrue(!p2.matcher(add4).find());

    }

    @Test
    public void testObj2Yaml() {

        Service.ServiceLoadBalancer lb = new Service.ServiceLoadBalancer();
        lb.setSimple("RANDOM");
        Service.ServiceLoadBalancer.ConsistentHash consistentHash = new Service.ServiceLoadBalancer.ConsistentHash();
        consistentHash.setHttpHeaderName("thisisheader");
        lb.setConsistentHash(consistentHash);

        Service.ServiceLoadBalancer.ConsistentHash.HttpCookie cookie = new Service.ServiceLoadBalancer.ConsistentHash.HttpCookie();
        cookie.setName("na");
        cookie.setPath("path");
        cookie.setTtl(30);

        consistentHash.setHttpCookie(cookie);

        System.out.println(CommonUtil.obj2yaml(lb));
    }
}
