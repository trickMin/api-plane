package org.hango.cloud.meta;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2022/11/18
 */
public enum PluginListenType {

    OUTBOUND("Outbound",0),

    INBOUND("Inbound",1),

    GATEWAY("Gateway",2);
    private String listenType;

    private Integer listenTypeValue;

    PluginListenType(String listenType, Integer listenTypeValue) {
        this.listenType = listenType;
        this.listenTypeValue = listenTypeValue;
    }

    public String getListenType() {
        return listenType;
    }

    public Integer getListenTypeValue() {
        return listenTypeValue;
    }

    public static String getListenType(Integer listenTypeValue) {
        for (PluginListenType value : values()) {
            if (value.listenType.equals(listenTypeValue)) {
                return value.listenType;
            }
        }
        return null;
    }


}
