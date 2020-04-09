package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.meta.IptablesConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static com.netease.cloud.nsf.service.SVMIptablesHelper.*;

/**
 * Created by 张武(zhangwu@corp.netease.com) at 2020/3/27
 */
public class SVMIptablesHelperTest {

	@Test
	public void case1() {
		IptablesConfig config = new IptablesConfig(
			true,
			null,
			null,
			Arrays.asList("7-500", "2-4", "8-1000", "6-7", "1", "1100"),
			false,
			null
		);
		String params = processIptablesConfigAndBuildParams(config);
		Assert.assertEquals("{\"enableOutbound\": true, \"outboundPorts\": [\":4\", \"6:1000\", \"1100\"], \"enableInbound\": false}", config.toString());
		Assert.assertEquals(INIT_PARAMS + " -i '*' -x '' -o '5,1001:1099,1101:' -b ''", params);
	}

	@Test
	public void case2() {
		IptablesConfig config = new IptablesConfig(
			false,
			null,
			null,
			Arrays.asList("7-500", "2-4", "8-1000", "6-7", "1", "1100"),
			true,
			Arrays.asList("7-500", "2-4", "8-1000", "6-7", "1", "1100")
		);
		String params = processIptablesConfigAndBuildParams(config);
		Assert.assertEquals("{\"enableOutbound\": false, \"enableInbound\": true, \"inboundPorts\": [\":4\", \"6:1000\", \"1100\"]}", config.toString());
		Assert.assertEquals(INIT_PARAMS + " -i '' -x '' -b ':4,6:1000,1100'", params);
		System.out.println("params are: " + params);
	}

	@Test
	public void overriding() {
		//指定出流量端口及ip、但是关闭出流量拦截
		IptablesConfig config = new IptablesConfig(
			false,
			Arrays.asList("1.1.1.1"),
			Arrays.asList("2.2.2.2"),
			Arrays.asList("7-500", "2-4", "8-1000", "6-7", "1", "1100"),
			false,
			Arrays.asList("78", "90")
		);
		String params = processIptablesConfigAndBuildParams(config);
		Assert.assertEquals("{\"enableOutbound\": false, \"enableInbound\": false}", config.toString());
		Assert.assertEquals(INIT_PARAMS + " -i '' -x '' -b ''", params);
		Assert.assertNull(config.getOutboundIps());
		Assert.assertNull(config.getExcludeOutboundIps());
		Assert.assertNull(config.getOutboundPorts());
		Assert.assertNull(config.getInboundPorts());

		IptablesConfig config1 = new IptablesConfig(
			false,
			null,
			null,
			null,
			true,
			Arrays.asList()
		);
		String params1 = processIptablesConfigAndBuildParams(config1);
		Assert.assertEquals("{\"enableOutbound\": false, \"enableInbound\": false}", config1.toString());
		Assert.assertEquals(INIT_PARAMS + " -i '' -x '' -b ''", params1);
		Assert.assertNull(config1.getOutboundIps());
		Assert.assertNull(config1.getExcludeOutboundIps());
		Assert.assertNull(config1.getOutboundPorts());
		Assert.assertNull(config1.getInboundPorts());
		Assert.assertEquals(config.toString(), config1.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void err1() {
		IptablesConfig config = new IptablesConfig(
			true,
			null,
			null,
			Arrays.asList("-1"),
			false,
			null
		);
		String params = processIptablesConfigAndBuildParams(config);
	}

	@Test(expected = IllegalArgumentException.class)
	public void err2() {
		IptablesConfig config = new IptablesConfig(
			true,
			null,
			null,
			Arrays.asList("65536"),
			false,
			null
		);
		String params = processIptablesConfigAndBuildParams(config);
	}

	@Test(expected = IllegalArgumentException.class)
	public void err3() {
		IptablesConfig config = new IptablesConfig(
			true,
			Arrays.asList("1.1.1.256"),
			null,
			null,
			false,
			null
		);
		String params = processIptablesConfigAndBuildParams(config);
	}
}
