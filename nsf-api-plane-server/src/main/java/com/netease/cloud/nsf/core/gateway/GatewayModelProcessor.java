package com.netease.cloud.nsf.core.gateway;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.operator.IntegratedResourceOperator;
import com.netease.cloud.nsf.meta.APIModel;
import com.netease.cloud.nsf.util.K8sResourceEnum;
import com.netease.cloud.nsf.util.PathExpressionEnum;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.ExceptionConst;
import me.snowdrop.istio.api.IstioResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/17
 **/
@Component
public class GatewayModelProcessor {


    @Autowired
    IntegratedResourceOperator operator;

    @Autowired
    EditorContext editorContext;

    /**
     * 将api转换为istio对应的规则
     *
     * @param api
     * @param namespace
     * @return
     */
    public List<IstioResource> translate(APIModel api, String namespace) {

        //TODO

        return null;
    }

    /**
     * 合并两个crd
     *
     * @param old
     * @param fresh
     * @return
     */
    public IstioResource merge(IstioResource old, IstioResource fresh) {

        if (fresh == null) return old;
        if (old == null) throw new ApiPlaneException(ExceptionConst.RESOURCE_NON_EXIST);
        return operator.merge(old, fresh);
    }

    /**
     * 在已有的istio crd中删去对应api部分
     *
     * @param old
     * @param api
     * @return
     */
    public IstioResource subtract(IstioResource old, String api) {
        K8sResourceEnum resource = K8sResourceEnum.get(old.getKind());
        switch (resource) {
            case VirtualService: {
                ResourceGenerator gen = ResourceGenerator.newInstance(old, ResourceType.OBJECT, editorContext);
                gen.removeElement(PathExpressionEnum.REMOVE_VS_HTTP.translate(api));
                return (IstioResource) gen.object(resource.mappingType());
            }
            case DestinationRule: {
                ResourceGenerator gen = ResourceGenerator.newInstance(old, ResourceType.OBJECT, editorContext);
                gen.removeElement(PathExpressionEnum.REMOVE_DST_SUBSET.translate(api));
                return (IstioResource) gen.object(resource.mappingType());
            }
            default:
                return old;
        }
    }
}
