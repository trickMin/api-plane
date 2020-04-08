package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.meta.IptablesConfig;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by 张武(zhangwu@corp.netease.com) at 2020/3/27
 */
public class SVMIptablesHelper {

	private final static Pattern IPRegexp = Pattern.compile("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}(/\\d{1,2})?");
	private final static int MIN_PORT = 1;
	private final static int MAX_PORT = 65535;
	final static String INIT_PARAMS = "-p '15001' -z '15006' -u '1337' -m REDIRECT -d '15020'";

	public static String processIptablesConfigAndBuildParams(IptablesConfig config) {
		StringBuilder sb = new StringBuilder(INIT_PARAMS);
		if (config.isEnableOutbound()) {
			config.setOutboundIps(processIps(config.getOutboundIps()));
			config.setExcludeOutboundIps(processIps(config.getExcludeOutboundIps()));
			List<int[]> outboundPorts = extractPorts(config.getOutboundPorts());
			config.setOutboundPorts(translatePorts(outboundPorts));
			appendValueList(sb, config.getOutboundIps(), "-i", "*");
			appendValueList(sb, config.getExcludeOutboundIps(), "-x", "");
			appendValueList(sb, translatePorts(invertPorts(outboundPorts)), "-o", null);
		} else {
			config.setOutboundIps(null);
			config.setExcludeOutboundIps(null);
			config.setOutboundPorts(null);
			sb.append(" -i '' -x ''");
		}
		if (config.isEnableInbound()) {
			config.setInboundPorts(translatePorts(extractPorts(config.getInboundPorts())));
			appendValueList(sb, config.getInboundPorts(), "-b", "");
			if (config.getInboundPorts() == null) {
				config.setEnableInbound(false);
			}
		} else {
			config.setInboundPorts(null);
			sb.append(" -b ''");
		}
		return sb.toString();
	}

	static void appendValueList(StringBuilder sb, List<String> values, String paramKey, String emptyValue) {
		if (values == null && emptyValue == null) {
			return;
		}
		sb.append(" ");
		sb.append(paramKey);
		sb.append(" '");
		sb.append(values != null ? String.join(",", values) : emptyValue);
		sb.append("'");
	}

	static List<String> processIps(List<String> ips) {
		if (CollectionUtils.isEmpty(ips)) {
			return null;
		}
		for (String ip : ips) {
			if (!IPRegexp.matcher(ip).matches()) {
				throw new IllegalArgumentException(String.format("invalid IP or CIDR: '%s'", ip));
			}
		}
		ArrayList<String> result = new ArrayList<>();
		Collections.sort(result);
		return result;
	}

	static List<int[]> extractPorts(List<String> ports) {
		if (CollectionUtils.isEmpty(ports)) {
			return null;
		}
		List<int[]> portsResult = new ArrayList<>();
		for (String portStr : ports) {
			if (portStr == null || !portStr.matches("\\d+(-\\d+)?")) {
				throw new IllegalArgumentException(String.format("invalid port format: '%s'", portStr));
			}
			int min, max;
			if (portStr.contains("-")) {
				String[] minMax = portStr.split("-");
				min = Integer.parseInt(minMax[0]);
				max = Integer.parseInt(minMax[1]);
			} else {
				min = max = Integer.parseInt(portStr);
			}
			if (min > max || min < MIN_PORT || max > MAX_PORT) {
				throw new IllegalArgumentException(String.format("invalid port range: '%s'", portStr));
			}
			portsResult.add(new int[]{min, max});
		}
		portsResult.sort(Comparator.comparing(ints -> ints[0]));
		int i = 0;
		while (i + 1 < portsResult.size()) {
			int[] left = portsResult.get(i);
			int[] right = portsResult.get(i + 1);
			if (left[1] + 1 >= right[0]) {
				left[1] = right[1];
				portsResult.remove(i + 1);
			} else {
				i++;
			}
		}
		return portsResult;
	}

	static List<int[]> invertPorts(List<int[]> ports) {
		if (ports == null) {
			return null;
		}
		List<int[]> result = new ArrayList<>();
		int minimum = ports.get(0)[0];
		if (minimum > MIN_PORT) {
			result.add(new int[]{1, minimum-1});
		}
		for (int i = 0; i + 1 < ports.size(); i++) {
			result.add(new int[]{ports.get(i)[1] + 1, ports.get(i+1)[0]-1});
		}
		int maximum = ports.get(ports.size() - 1)[1];
		if (maximum < MAX_PORT) {
			result.add(new int[]{maximum+1, MAX_PORT});
		}
		return result;
	}

	static List<String> translatePorts(List<int[]> ports) {
		if (ports == null) {
			return null;
		}
		return ports.stream()
			.map(ints -> {
				if (ints[0] == ints[1]) {
					return "" + ints[0];
				} else if (ints[0] == MIN_PORT) {
					return ":" + ints[1];
				} else if (ints[1] == MAX_PORT) {
					return ints[0] + ":";
				} else {
					return ints[0] + ":" + ints[1];
				}
			})
			.collect(Collectors.toList());
	}

}
