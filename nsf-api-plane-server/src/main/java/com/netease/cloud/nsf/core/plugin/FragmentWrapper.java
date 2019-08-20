package com.netease.cloud.nsf.core.plugin;

import com.netease.cloud.nsf.util.K8sResourceEnum;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/19
 **/
public class FragmentWrapper {
    private K8sResourceEnum resourceType;
    private FragmentTypeEnum fragmentType;
    private String content;

    public K8sResourceEnum getResourceType() {
        return resourceType;
    }

    public void setResourceType(K8sResourceEnum resourceType) {
        this.resourceType = resourceType;
    }

    public FragmentTypeEnum getFragmentType() {
        return fragmentType;
    }

    public void setFragmentType(FragmentTypeEnum fragmentType) {
        this.fragmentType = fragmentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static class Builder {
        private K8sResourceEnum resourceType;
        private FragmentTypeEnum fragmentType;
        private String content;

        public Builder withResourceType(K8sResourceEnum resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public Builder withFragmentType(FragmentTypeEnum fragmentType) {
            this.fragmentType = fragmentType;
            return this;
        }

        public Builder withContent(String content) {
            this.content = content;
            return this;
        }

        public FragmentWrapper build() {
            FragmentWrapper wrapper = new FragmentWrapper();
            wrapper.setResourceType(resourceType);
            wrapper.setFragmentType(fragmentType);
            wrapper.setContent(content);
            return wrapper;
        }
    }
}
