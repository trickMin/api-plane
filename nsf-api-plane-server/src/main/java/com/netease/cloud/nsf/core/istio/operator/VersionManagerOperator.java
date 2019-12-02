package com.netease.cloud.nsf.core.istio.operator;

import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.meta.PodStatus;
import com.netease.cloud.nsf.meta.PodVersion;
import com.netease.cloud.nsf.util.function.Equals;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;


@Component
public class VersionManagerOperator implements IstioResourceOperator<VersionManager> {
    @Override
    public VersionManager merge(VersionManager old, VersionManager fresh) {
        VersionManager versionManager = new VersionManagerBuilder(old).build();
        List<SidecarVersionSpec> oldSpecList  = old.getSpec().getSidecarVersionSpec();
        List<SidecarVersionSpec> latestSpecList  = fresh.getSpec().getSidecarVersionSpec();
        versionManager.getSpec().setSidecarVersionSpec(mergeList(oldSpecList, latestSpecList, new SidecarVersionSpecEquals()));
        versionManager.getSpec().setStatus(old.getSpec().getStatus());

        return versionManager;
    }

    private class SidecarVersionSpecEquals implements Equals<SidecarVersionSpec> {
        @Override
        public boolean apply(SidecarVersionSpec np, SidecarVersionSpec op) {
            return Objects.equals(op.getSelector(), np.getSelector());
        }
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.VersionManager.name().equals(name);
    }

    @Override
    public VersionManager subtract(VersionManager old, String value) {
        // TODO
        return old;
    }


    @Override
    public boolean isUseless(VersionManager versionManager) {
        return versionManager.getSpec() == null ||
                StringUtils.isEmpty(versionManager.getApiVersion()) ||
                CollectionUtils.isEmpty(versionManager.getSpec().getSidecarVersionSpec());
    }

    public List<PodStatus> getPodVersion(PodVersion podVersion, VersionManager versionmanager) {

        List<PodStatus> resultList = new ArrayList<>();
        Status state = versionmanager.getSpec().getStatus();
        if (state == null || CollectionUtils.isEmpty(state.getPodVersionStatus())) {
            return resultList;
        }

        List<PodVersionStatus> versionList  = state.getPodVersionStatus();
        if (CollectionUtils.isEmpty(versionList)) {
            return resultList;
        }
        List<String> podList = podVersion.getPodNames();

        for (String need : podList) {
            for (PodVersionStatus had : versionList) {
                if (had.getPodName().equals(need)) {
                    resultList.add(
                            new PodStatus(had.getPodName(),
                            had.getCurrentVersion(),
                            had.getLastUpdateTime(),
                            had.getStatusCode(),
                            had.getStatusMessage()));
                }
            }
        }
        return resultList;
    }
}
