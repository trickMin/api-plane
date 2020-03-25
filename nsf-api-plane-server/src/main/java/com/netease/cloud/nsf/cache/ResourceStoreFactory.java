package com.netease.cloud.nsf.cache;

import java.util.*;

public class ResourceStoreFactory {

    public static Map<String, OwnerReferenceSupportStore> resourceStoreMap = new HashMap<>();


    public static OwnerReferenceSupportStore getResourceStore(String clusterId) {
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

    public static List<String> listNamespaceByClusterId(String clusterId){
        OwnerReferenceSupportStore store = resourceStoreMap.get(clusterId);
        return store.listNamespaces();
    }
}
