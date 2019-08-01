package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.javafx.binding.StringFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
public class WhiteList {

    private static final Logger logger = LoggerFactory.getLogger(WhiteList.class);
    private SiderCarRequestMeta siderCarMeta;

    @JsonProperty("sources")
    private List<String> sources;

    @JsonProperty("outWeight")
    private int outWeight;

    @JsonProperty("authPaths")
    private List<String> authPaths;

    @JsonProperty("allPaths")
    private List<String> allPaths;

    @JsonProperty("contextPath")
    private String contextPath;

    public void setSiderCarMeta(SiderCarRequestMeta siderCarMeta) {
        this.siderCarMeta = siderCarMeta;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public int getOutWeight() {
        return outWeight;
    }

    public void setOutWeight(int outWeight) {
        this.outWeight = outWeight;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath == null || contextPath.equals("/") ? "" : contextPath;
    }

    public String getService() {
        return siderCarMeta.getService();
    }

    public String getFullService() {
        return StringFormatter.format("%s.%s.svc.%s", siderCarMeta.getService(), siderCarMeta.getNamespace(), siderCarMeta.getCluster()).getValue();
    }

    public List<String> getAuthPaths() {
        return authPaths;
    }

    public void setAuthPaths(List<String> authPaths) {
        this.authPaths = authPaths;
    }

    public List<String> getAllPaths() {
        return allPaths;
    }

    public void setAllPaths(List<String> allPaths) {
        this.allPaths = allPaths;
    }

    public String getNamespace() {
        return siderCarMeta.getNamespace();
    }

    public String getSourcesNamespace() {
        return getNamespace();
    }

    public List<String> getConfigPassedPaths() {
        List<String> result = transformPaths(allPaths);
        for (String authPath : transformPaths(authPaths)) {
            result.removeIf(p -> p.startsWith(authPath));
        }
        result = finishTransformPath(result);
        logger.info("service: {}, granted paths: {}", getService(), result);
        return result;
    }

    public List<String> getConfigAuthPaths() {
        return finishTransformPath(transformPaths(authPaths));
    }

    private List<String> transformPaths(List<String> paths) {
        List<String> result = new ArrayList<>();
		for (String pth : paths) {
			String newPath = pth
                .replaceAll("\\{.*?}", "*")
                .replaceAll("\\*(/?\\*)+", "**")
                .replaceAll("\\*+(/?)$", "");
            logger.info("service: {}, pth: {}, newPath: {}", getService(), pth, newPath);
		    if (result.stream().anyMatch(path -> newPath.startsWith(newPath))) {
		        continue;
            }
			result.removeIf(path -> path.startsWith(newPath));

		    result.add(newPath);
		}
        logger.info("service: {}, original paths: {}, result: {}", getService(), paths, result);
		return result;
    }

    private List<String> finishTransformPath(List<String> result) {
        result = result.stream()
            .map(path -> path.replaceAll("^(/)?\\*+", ""))
            .distinct()
            .map(path -> getContextPath() + path)
            .collect(Collectors.toList());
        return result;
    }
}
