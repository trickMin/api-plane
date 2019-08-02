package com.netease.cloud.nsf.core.plugin;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/2
 **/
@Component("RewritePluginProcessor")
public class RewritePluginProcessor implements PluginProcessor {
    private static final Logger logger = LoggerFactory.getLogger(RewritePluginProcessor.class);

    @Autowired
    private KubernetesClient client;

    @Autowired
    private EditorContext editorContext;

    @Override
    public void process(Object serviceInfo, String plugin, String schema) {
        logger.info(plugin);
        logger.info(schema);
    }
}
