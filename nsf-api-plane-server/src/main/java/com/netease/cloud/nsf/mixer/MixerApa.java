package com.netease.cloud.nsf.mixer;

import com.netease.cloud.nsf.cache.ResourceCache;
import io.grpc.stub.StreamObserver;
import net.devh.springboot.autoconfigure.grpc.server.GrpcService;
import nsfmeta.HandleNsfmetaServiceGrpc;
import nsfmeta.TemplateHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.AntPathMatcher;

import java.util.stream.Collectors;


/**
 * Created by 张武(zhangwu@corp.netease.com) at 2020/2/4
 */
@GrpcService(HandleNsfmetaServiceGrpc.class)
public class MixerApa extends HandleNsfmetaServiceGrpc.HandleNsfmetaServiceImplBase {

	@Autowired private ResourceCache resourceCache;

	@Override
	public void handleNsfmeta(TemplateHandlerService.HandleNsfmetaRequest request, StreamObserver<TemplateHandlerService.OutputMsg> responseObserver) {
		TemplateHandlerService.InstanceMsg instance = request.getInstance();
		String clusterId = "default";
		PodInfo destPod = new PodInfo(clusterId, instance.getDestinationUid());
		PodInfo sourcePod = new PodInfo(clusterId, instance.getSourceUid());
		String urlPath = instance.getUrlPath();

		String destProject = testGetProjectId(destPod);
		String sourceProject = testGetProjectId(sourcePod);
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

	private String testGetProjectId(PodInfo pod) {
		if (pod.podName.hashCode() % 2 == 0) {
			return "test";
		} else {
			return "project1";
		}
	}

	private AntPathMatcher matcher = new AntPathMatcher();

	private String makeUrlPathPattern(String clusterId, PodInfo pod, String path) {
		return resourceCache.getMixerPathPatterns(clusterId, pod.namespace, pod.appName).stream()
			.filter(pattern -> matcher.match(pattern, path))
			.collect(Collectors.joining("|", "|", "|"));
	}

	private class PodInfo {
		private String namespace;
		private String appName;
		private String podName;

		// kubernetes://a-686ff98446-hcdlm.powerful-v13
		public PodInfo(String clusterId, String uid) {
			String fullPodName = uid.split("://")[1];
			int lastDot = fullPodName.lastIndexOf(".");

			podName = fullPodName.substring(0, lastDot);
			namespace = fullPodName.substring(lastDot + 1);
			appName = resourceCache.getAppNameByPod(clusterId, namespace, podName);
		}
	}
}

