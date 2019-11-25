package com.netease.cloud.nsf.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceStoreFactory {

    public static Map<String, OwnerReferenceSupportStore> resourceStoreMap = new HashMap<>();


    static OwnerReferenceSupportStore getResourceStore(String clusterId) {
        if (resourceStoreMap.get(clusterId) == null) {
            synchronized (ResourceStoreFactory.class) {
                if (resourceStoreMap.get(clusterId) == null) {
                    resourceStoreMap.putIfAbsent(clusterId, new OwnerReferenceSupportStore(new ResourceStore()));
                }
            }
        }
        return resourceStoreMap.get(clusterId);
    }

    static List<OwnerReferenceSupportStore> listStoreMap(){
        return new ArrayList<>(resourceStoreMap.values());
    }

    public static List<String> listClusterId(){
        return new ArrayList<>(resourceStoreMap.keySet());
    }

    static Map<String, OwnerReferenceSupportStore> getResourceStoreMap(){
        return resourceStoreMap;
    }
}
