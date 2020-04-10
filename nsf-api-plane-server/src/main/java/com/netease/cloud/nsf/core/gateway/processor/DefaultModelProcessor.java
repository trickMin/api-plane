package com.netease.cloud.nsf.core.gateway.processor;

import com.netease.cloud.nsf.core.gateway.handler.DataHandler;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.core.template.TemplateTranslator;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/27
 **/
public class DefaultModelProcessor<T> implements ModelProcessor<T> {

    TemplateTranslator templateTranslator;

    public DefaultModelProcessor(TemplateTranslator templateTranslator) {
        this.templateTranslator = templateTranslator;
    }

    @Override
    public List<String> process(String template, T t, DataHandler<T> dataHandler) {

        List<TemplateParams> params = dataHandler.handle(t);
        if (CollectionUtils.isEmpty(params)) return Collections.emptyList();

        return process(template, params);
    }

    @Override
    public String process(String template, TemplateParams params) {
        return templateTranslator.translate(template, params.output());
    }

    @Override
    public List<String> process(String template, List<TemplateParams> params) {

        if (CollectionUtils.isEmpty(params)) return Collections.emptyList();

        return params.stream()
                .map(p -> templateTranslator.translate(template, p.output()))
                .filter(r -> !StringUtils.isEmpty(r))
                .collect(Collectors.toList());
    }
}
