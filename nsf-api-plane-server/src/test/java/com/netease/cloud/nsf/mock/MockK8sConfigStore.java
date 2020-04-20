package com.netease.cloud.nsf.mock;

import com.netease.cloud.nsf.core.k8s.MultiClusterK8sClient;
import com.netease.cloud.nsf.core.servicemesh.MultiK8sConfigStore;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/17
 **/
public class MockK8sConfigStore extends MultiK8sConfigStore {

    private Map<ResourceId, HasMetadata> store = new HashMap<>();

    public MockK8sConfigStore(MultiClusterK8sClient multiClient) {
        super(multiClient);
    }

    public MockK8sConfigStore() {
        super(null);
    }

    @Override
    public void delete(HasMetadata resource) {
        store.remove(getId(resource));
    }

    @Override
    public void update(HasMetadata resource) {
        store.put(getId(resource), resource);
    }

    @Override
    public HasMetadata get(HasMetadata resource) {
        return store.get(getId(resource));
    }

    @Override
    public HasMetadata get(String kind, String namespace, String name) {
        return store.get(getId(kind, name, namespace));
    }

    @Override
    public List<HasMetadata> get(String kind, String namespace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<HasMetadata> get(String kind, String namespace, Map<String, String> labels) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void supply(HasMetadata resource) {
        // do nothing
    }

    public int size() {
        return store.size();
    }

    private ResourceId getId(HasMetadata hasMetadata) {
        return new ResourceId(hasMetadata.getKind(), hasMetadata.getMetadata().getName(), hasMetadata.getMetadata().getNamespace());
    }

    private ResourceId getId(String kind, String name, String namespace) {
        return new ResourceId(kind, name, namespace);
    }

    class ResourceId {
        private String kind;
        private String name;
        private String namespace;

        public ResourceId(String kind, String name, String namespace) {
            this.kind = kind;
            this.name = name;
            this.namespace = namespace;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ResourceId that = (ResourceId) o;
            return Objects.equals(kind, that.kind) &&
                    Objects.equals(name, that.name) &&
                    Objects.equals(namespace, that.namespace);
        }

        @Override
        public int hashCode() {
            return Objects.hash(kind, name, namespace);
        }
    }

}
