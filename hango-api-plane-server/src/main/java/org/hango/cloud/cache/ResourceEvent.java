package org.hango.cloud.cache;/**
* @Author: zhufengwei.sx
* @Date: 2022/8/26 14:41
**/
public class ResourceEvent {

    private String resource;
    private String eventType;

    ResourceEvent(){

    }

    ResourceEvent(String eventType, String resource){
        this.resource = resource;
        this.eventType = eventType;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}