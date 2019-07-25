package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.meta.template.ServiceMeshTemplate;
import com.netease.cloud.nsf.service.TemplateService;
import com.netease.cloud.nsf.service.WhiteListService;
import freemarker.template.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
public class WhiteListServiceImpl implements WhiteListService {
    private static final Logger logger = LoggerFactory.getLogger(WhiteListServiceImpl.class);

    @Autowired
    private EditorContext editorContext;

    @Autowired
    private Configuration configuration;

    @Autowired
    private TemplateService templateService;

    @Override
    public void createTargetService(ServiceMeshTemplate template) {
    }

    @Override
    public void deleteTargetService() {

    }

    @Override
    public void addSourceService() {

    }

    @Override
    public void removeSourceService() {

    }
}
