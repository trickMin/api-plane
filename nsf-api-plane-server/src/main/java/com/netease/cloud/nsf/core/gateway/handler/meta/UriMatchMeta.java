package com.netease.cloud.nsf.core.gateway.handler.meta;

import com.netease.cloud.nsf.meta.UriMatch;

public class UriMatchMeta {
    /**
     * uriMatch信息
     */
    private UriMatch uriMatch;
    /**
     * uri信息
     */
    private String uri;

    public UriMatch getUriMatch() {
        return uriMatch;
    }

    public void setUriMatch(UriMatch uriMatch) {
        this.uriMatch = uriMatch;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public UriMatchMeta(UriMatch uriMatch, String uri) {
        this.uriMatch = uriMatch;
        this.uri = uri;
    }
}
