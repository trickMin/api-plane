package com.netease.cloud.nsf.mixer;

import com.google.common.base.Strings;
import com.netease.cloud.nsf.cache.ResourceCache;
import com.netease.cloud.nsf.service.ServiceMeshService;
import io.grpc.stub.StreamObserver;
import net.devh.springboot.autoconfigure.grpc.server.GrpcService;
import nsfmeta.HandleNsfmetaServiceGrpc;
import nsfmeta.TemplateHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.stream.Collectors;


/**
 * Created by 张武(zhangwu@corp.netease.com) at 2020/2/4
 */
@GrpcService(HandleNsfmetaServiceGrpc.class)
public class MixerApa extends HandleNsfmetaServiceGrpc.HandleNsfmetaServiceImplBase {

	@Autowired private ResourceCache resourceCache;
	@Autowired private ServiceMeshService serviceMeshService;
	private static final Logger logger = LoggerFactory.getLogger(MixerApa.class);

	@Override
	public void handleNsfmeta(TemplateHandlerService.HandleNsfmetaRequest request, StreamObserver<TemplateHandlerService.OutputMsg> responseObserver) {
		TemplateHandlerService.InstanceMsg instance = request.getInstance();
		String clusterId = "default";
		PodInfo destPod = new PodInfo(clusterId, instance.getDestinationUid());
		PodInfo sourcePod = new PodInfo(clusterId, instance.getSourceUid());
		String urlPath = instance.getUrlPath();

		String destProject = getProjectId(destPod);
		String sourceProject = getProjectId(sourcePod);
		String patternsStr = makeUrlPathPattern(clusterId, destPod, urlPath);
		TemplateHandlerService.OutputMsg output = TemplateHandlerService.OutputMsg.newBuilder()
			.setDestinationProject(destProject)
			.setSourceProject(sourceProject)
			.setUrlPathPattern(urlPath)
			.setUrlPathPattern(patternsStr)
			.build();
		System.out.println(String.format("############################################## %s#%s\n%s", sourcePod.podName, destPod.podName, output));
		responseObserver.onNext(output);
		responseObserver.onCompleted();
	}

	private String getProjectId(PodInfo pod) {
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
			} else {
				namespace = appName = podName = "";
				if (!StringUtils.isEmpty(uid)) {
					logger.error("invalid uid: {}", uid);
				}
			}
		}
	}
}

