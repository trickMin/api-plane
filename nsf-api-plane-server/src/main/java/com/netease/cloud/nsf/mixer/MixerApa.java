package com.netease.cloud.nsf.mixer;

import com.google.common.base.Strings;
import com.netease.cloud.nsf.cache.ResourceCache;
import com.netease.cloud.nsf.core.servicemesh.ServiceMeshConfigManager;
import com.netease.cloud.nsf.service.ServiceMeshService;
import io.grpc.stub.StreamObserver;
import nsfmeta.HandleNsfmetaServiceGrpc;
import nsfmeta.TemplateHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.stream.Collectors;


/**
 * Created by 张武(zhangwu@corp.netease.com) at 2020/2/4
 */
public class MixerApa extends HandleNsfmetaServiceGrpc.HandleNsfmetaServiceImplBase {

	@Autowired private ResourceCache resourceCache;
	@Autowired private ServiceMeshService serviceMeshService;
	@Autowired private ServiceMeshConfigManager serviceMeshConfigManager;
	private static final Logger logger = LoggerFactory.getLogger(MixerApa.class);

	@Value(value = "${enableASG:false}")
	private boolean enableASG;

	@Override
	public void handleNsfmeta(TemplateHandlerService.HandleNsfmetaRequest request, StreamObserver<TemplateHandlerService.OutputMsg> responseObserver) {
		TemplateHandlerService.InstanceMsg instance = request.getInstance();
		String host = instance.getHost();
		String destinationHost = instance.getDestinationHost();
		String xNsfApp = instance.getXNsfApp();
//		System.out.println(String.format("host: %s, destinationHost: %s, xNsfApp: %s", host, destinationHost, xNsfApp));
		String clusterId = "default";
		PodInfo destPod = new PodInfo(clusterId, instance.getDestinationUid());
		PodInfo sourcePod = new PodInfo(clusterId, instance.getSourceUid());
		String urlPath = instance.getUrlPath();
		if (enableASG) {
			recordCallRelation(sourcePod, xNsfApp, host, destinationHost);
		}

		String destProject = getProjectId(destPod);
		String sourceProject = getProjectId(sourcePod);
		String patternsStr = makeUrlPathPattern(clusterId, destPod, urlPath);
		TemplateHandlerService.OutputMsg output = TemplateHandlerService.OutputMsg.newBuilder()
			.setDestinationProject(destProject)
			.setSourceProject(sourceProject)
			.setUrlPathPattern(patternsStr)
			.build();
		responseObserver.onNext(output);
		responseObserver.onCompleted();
	}

	private void recordCallRelation(PodInfo sourcePod, String xNsfAppHeader, String authority, String destinationHost) {
		ArrayList<String> targetHosts = new ArrayList<>();
		if(!StringUtils.isEmpty(authority)) {
			if (authority.contains(".")) {
				targetHosts.add(authority);
			}
			String[] hostParts = authority.split("\\.");
			if (hostParts.length == 1) {
				targetHosts.add(String.format("%s.%s.svc.cluster.local", hostParts[0], sourcePod.namespace));
			} else if (hostParts.length == 2) {
				targetHosts.add(String.format("%s.%s.svc.cluster.local", hostParts[0], hostParts[1]));
			}
		}
		if (!StringUtils.isEmpty(destinationHost)) {
			targetHosts.add(destinationHost);
		}

		final String sourceApp;
		final String sourceNamespace;
		if (sourcePod.isIngress) {
			int idx = xNsfAppHeader.lastIndexOf(".");
			if (idx != -1) {
				sourceApp = xNsfAppHeader.substring(0, idx);
				sourceNamespace = xNsfAppHeader.substring(idx + 1);
			} else {
				sourceApp = sourceNamespace = "";
			}
		} else {
			sourceApp = sourcePod.appName;
			sourceNamespace = sourcePod.namespace;
		}
		logger.info("recording call relation: sourceApp: {}, sourceNamespace: {}, targetHosts: {}, isIngress: {}", sourceApp, sourceNamespace, targetHosts, sourcePod.isIngress);

		if (!StringUtils.isEmpty(sourceApp) && !StringUtils.isEmpty(sourceNamespace)) {
			for (String targetHost : targetHosts) {
				serviceMeshConfigManager.updateSidecarScope(sourceApp, sourceNamespace, targetHost);
			}
		}

	}

	private String getProjectId(PodInfo pod) {
		if (pod.appName.equals("")) {
			return "";
		}
		return Strings.nullToEmpty(serviceMeshService.getProjectCodeByApp(pod.namespace, pod.appName, pod.clusterId));
	}

	private AntPathMatcher matcher = new AntPathMatcher();

	private String makeUrlPathPattern(String clusterId, PodInfo pod, String path) {
		return resourceCache.getMixerPathPatterns(clusterId, pod.namespace, pod.appName).stream()
			.filter(pattern -> matcher.match(pattern, path))
			.collect(Collectors.joining("|", "|", "|"));
	}

	private class PodInfo {
		@NotNull private final String clusterId;
		@NotNull private final String namespace;
		@NotNull private final String appName;
		@NotNull private final String podName;
		private final boolean isIngress;

		// kubernetes://a-686ff98446-hcdlm.powerful-v13
		public PodInfo(String clusterId, String uid) {
			if (clusterId == null) {
				throw new IllegalArgumentException("cluster id can't be null");
			}
			this.clusterId = clusterId;
			if (!StringUtils.isEmpty(uid) && uid.contains("://") && uid.contains(".")) {
				String fullPodName = uid.split("://")[1];
				int lastDot = fullPodName.lastIndexOf(".");
				podName = fullPodName.substring(0, lastDot);
				namespace = fullPodName.substring(lastDot + 1);
				appName = resourceCache.getAppNameByPod(clusterId, namespace, podName);
				isIngress = "true".equals(resourceCache.getPodLabel(clusterId, namespace, podName, "nsf.skiff.netease.com/isIngress"));
			} else {
				namespace = appName = podName = "";
				isIngress = false;
				if (!StringUtils.isEmpty(uid)) {
					logger.error("invalid uid: {}", uid);
				}
			}
		}
	}
}

