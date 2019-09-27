package com.netease.cloud.nsf.core.gateway.processor;

import com.netease.cloud.nsf.core.gateway.handler.DataHandler;
import com.netease.cloud.nsf.core.template.TemplateParams;

import java.util.List;

public interface ModelProcessor<T> {

    List<String> process(String template, T t, DataHandler<T> dataHandler);

    String process(String template, TemplateParams params);

    List<String> processSource(String source, T t, DataHandler<T> dataHandler);

    String processSource(String source, TemplateParams params);
}
