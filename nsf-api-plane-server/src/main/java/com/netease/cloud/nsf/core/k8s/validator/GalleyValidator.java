package com.netease.cloud.nsf.core.k8s.validator;

import com.netease.cloud.nsf.core.galley.GalleyHttpClient;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import io.fabric8.kubernetes.api.model.admission.AdmissionRequest;
import io.fabric8.kubernetes.api.model.admission.AdmissionReview;
import me.snowdrop.istio.api.IstioResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.netease.cloud.nsf.core.k8s.K8sResourceEnum.*;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/3/5
 **/
@Component
public class GalleyValidator implements K8sResourceValidator<IstioResource> {

    private static final EnumSet<K8sResourceEnum> validEnum = EnumSet.of(
            VirtualService,
            DestinationRule,
            Gateway,
            ServiceEntry
    );

    private GalleyHttpClient galleyHttpClient;

    @Autowired
    public GalleyValidator(GalleyHttpClient galleyHttpClient) {
        this.galleyHttpClient = galleyHttpClient;
    }

    @Override
    public boolean adapt(String name) {
        return validEnum.contains(K8sResourceEnum.get(name));
    }

    @Override
    public Set<ConstraintViolation<IstioResource>> validate(IstioResource var) {
        Set<ConstraintViolation<IstioResource>> violations = new LinkedHashSet<>();
        AdmissionReview review = new AdmissionReview();
        AdmissionRequest request = new AdmissionRequest();
        request.setObject(var);
        request.setOperation("CREATE");
        review.setRequest(request);
        AdmissionReview response = galleyHttpClient.admitpilot(review);
        if (Boolean.FALSE.equals(response.getResponse().getAllowed())) {
            violations.add(new ConstraintViolation(response.getResponse().getStatus().getMessage(), var, this));
        }
        return violations;
    }
}
