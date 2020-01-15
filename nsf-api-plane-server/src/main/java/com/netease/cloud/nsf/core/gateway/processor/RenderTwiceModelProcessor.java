package com.netease.cloud.nsf.core.gateway.processor;

import com.netease.cloud.nsf.core.gateway.handler.DataHandler;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.core.template.TemplateTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/27
 **/
@Component
public class RenderTwiceModelProcessor<T> implements ModelProcessor<T> {

    @Autowired
    private TemplateTranslator templateTranslator;

    @Override
    public List<String> process(String template, T t, DataHandler<T> dataHandler) {

        List<TemplateParams> params = dataHandler.handle(t);
        if (CollectionUtils.isEmpty(params)) return Collections.emptyList();

        return params.stream()
                .map(p -> templateTranslator.translate("tmp",
                        templateTranslator.translate(template, p.output()),
                        p.output()))
                .filter(r -> !StringUtils.isEmpty(r))
                .collect(Collectors.toList());
    }

    @Override
    public String process(String template, TemplateParams params) {
        throw new UnsupportedOperationException();
    }

}
