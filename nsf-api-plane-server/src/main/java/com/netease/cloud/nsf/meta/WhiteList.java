package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.javafx.binding.StringFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
public class WhiteList {

    private static final Logger logger = LoggerFactory.getLogger(WhiteList.class);

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

    private String service;
    private String namespace;

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
    	this.contextPath = contextPath;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getService() {
        return service;
    }

    public String getFullService() {
        return StringFormatter.format("%s.%s.svc.cluster.local", service, namespace).getValue();
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
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getSourcesNamespace() {
        return getNamespace();
    }

    public List<VerboseSource> getVerboseSources() {
        return getSources().stream()
            .map(source -> {
                String[] splits = source.split("\\.");
                if (splits.length != 2) {
                    logger.error("source service format error: {}", source);
                }
                return new VerboseSource(splits[0], splits[1]);
            })
            .collect(Collectors.toList());
    }

    public List<String> getConfigPassedPaths() {
        List<String> result = simplifyPaths(allPaths);
        for (String authPath : simplifyPaths(authPaths)) {
            result.removeIf(p -> p.startsWith(authPath));
        }
        result = finishTransformPath(result);
        logger.info("service: {}, granted paths: {}", getService(), result);
        return result;
    }

    private List<String> simplifyPaths(List<String> authPaths) {
        return authPaths.stream().map(path -> {
            String simplified = path
                .replaceAll("\\{.*?}", "*")
                .replaceAll("\\*(/?\\*)+", "**")
                .replaceAll("\\*+(/?)$", "");
            logger.info("service: {}, path: {}, simplified: {}", "", path, simplified);
            return simplified;
        }).collect(Collectors.toList());
    }

    private List<String> finishTransformPath(List<String> paths) {
        contextPath = contextPath == null || contextPath.equals("/") ? "" : contextPath;
        return paths.stream()
			.filter(path -> !path.isEmpty())
            .filter(path -> paths.stream().noneMatch(p -> p.length() < path.length() && path.startsWith(p))) // /a和/a/b/c只保留/a
            .map(path -> path.replaceAll("^(/)?\\*+", ""))
            .distinct()
            .map(path -> contextPath + path)
            .collect(Collectors.toList());
    }

//    public static void main(String[] args) {
//        WhiteList wl = new WhiteList();
//        wl.setAllPaths(Arrays.asList("/error", "/provider/version/a/**", "/consumer/getProviderVersion/{a}/{b}/*", "/provider/changeVersion", "/provider/unauthVersion", "/provider/version"));
//        wl.setAuthPaths(Arrays.asList("/provider/version"));
//        wl.siderCarMeta = new SiderCarRequestMeta();//.setService("aa");
//		List<String> configPassedPaths = wl.getConfigPassedPaths();
//		configPassedPaths.stream();
//    }

    public static class VerboseSource {
        private String name;
        private String namespace;

        public VerboseSource(String name, String namespace) {
            this.name = name;
            this.namespace = namespace;
        }

        public VerboseSource() {}

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }
    }
}
