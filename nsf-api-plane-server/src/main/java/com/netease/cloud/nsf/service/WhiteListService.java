package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.meta.template.ServiceMeshTemplate;

/**
 * 几个概念:
 * 1. SourceService: 源服务，访问TargetService的服务
 * 2. TargetService: 目标服务，白名单访问策略在这一端配置
 * 3. User: 访问角色
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
public interface WhiteListService {

    /**
     * 创建一个关于TargetService的访问策略
     */
    void createTargetService(ServiceMeshTemplate template);

    void deleteTargetService();

    /**
     * 添加一个SourceService到TargetService的白名单中
     */
    void addSourceService();

    /**
     * 讲一个SourceService从TargetService移除
     */
    void removeSourceService();
}
