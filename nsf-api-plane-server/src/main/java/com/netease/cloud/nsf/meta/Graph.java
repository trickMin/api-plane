package com.netease.cloud.nsf.meta;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/12/12
 **/
public class Graph {

    private Long timestamp;

    private Long duration;

    private String graphType;

    private Elements elements;

    public class Elements {

        private List<Object> nodes;
        private List<Object> edges;

        public List<Object> getNodes() {
            return nodes;
        }

        public void setNodes(List<Object> nodes) {
            this.nodes = nodes;
        }

        public List<Object> getEdges() {
            return edges;
        }

        public void setEdges(List<Object> edges) {
            this.edges = edges;
        }
    }


    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getGraphType() {
        return graphType;
    }

    public void setGraphType(String graphType) {
        this.graphType = graphType;
    }

    public Elements getElements() {
        return elements;
    }

    public void setElements(Elements elements) {
        this.elements = elements;
    }
}