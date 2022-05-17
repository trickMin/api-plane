package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import istio.networking.v1alpha3.EnvoyFilterOuterClass;
import istio.networking.v1alpha3.SidecarOuterClass;

import java.util.List;

/**
 * EnvoyFilter dto
 *
 * @author xin li
 * @date 2022/5/13 14:29
 */
public class EnvoyFilterOrderDTO {

    @JsonProperty("ConfigPatches")
    private List<EnvoyFilterOuterClass.EnvoyFilter.EnvoyConfigObjectPatch> configPatches;

    @JsonProperty(value = "WorkloadSelector")
    private SidecarOuterClass.WorkloadSelector workloadSelector;

    public List<EnvoyFilterOuterClass.EnvoyFilter.EnvoyConfigObjectPatch> getConfigPatches() {
        return configPatches;
    }

    public void setConfigPatches(List<EnvoyFilterOuterClass.EnvoyFilter.EnvoyConfigObjectPatch> configPatches) {
        this.configPatches = configPatches;
    }

    public SidecarOuterClass.WorkloadSelector getWorkloadSelector() {
        return workloadSelector;
    }

    public void setWorkloadSelector(SidecarOuterClass.WorkloadSelector workloadSelector) {
        this.workloadSelector = workloadSelector;
    }
}
