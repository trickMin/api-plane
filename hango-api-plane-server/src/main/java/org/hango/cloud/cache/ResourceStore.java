package org.hango.cloud.cache;

import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
* @Author: zhufengwei.sx
* @Date: 2022/8/26 14:40
**/
public class ResourceStore  {
    protected ConcurrentHashMap<String, HasMetadata> resourceStore = new ConcurrentHashMap<>();


    public HasMetadata get(String name) {
        return resourceStore.get(name);
    }

    public void add(String name, HasMetadata obj, String resourceVersion, boolean concurrent) {
        update(name, obj, resourceVersion, concurrent);
    }

    public void update(String name, HasMetadata obj, String resourceVersion, boolean concurrent) {
        if (concurrent && !validateVersion(name, resourceVersion)){
            return;
        }
        resourceStore.put(name, obj);
    }

    public void delete(String name, String resourceVersion, boolean concurrent) {
        if (concurrent && !validateVersion(name, resourceVersion)){
            return;
        }
        resourceStore.remove(name);
    }



    /**
     * 通过resourceversion实现乐观锁
     */
    private boolean validateVersion(String name, String resourceVersion){
        HasMetadata hasMetadata = resourceStore.get(name);
        if (hasMetadata == null){
            return true;
        }
        long cacheVersion = Long.parseLong(hasMetadata.getMetadata().getResourceVersion());
        long currentVersion = Long.parseLong(resourceVersion);
        return currentVersion > cacheVersion;
    }


    public List<HasMetadata> list() {
        return new ArrayList<>(resourceStore.values());
    }

}
