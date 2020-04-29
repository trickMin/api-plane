package com.netease.cloud.nsf.mcp.dao;

import com.netease.cloud.nsf.mcp.dao.meta.Resource;

import java.util.List;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/22
 **/
public interface ResourceDao {
    boolean contains(String collection, String name);

    void add(Resource resource);

    void delete(String collection, String name);

    void update(Resource resource);

    Resource get(String collection, String name);

    List<Resource> list();

    List<Resource> list(String collection);

    List<Resource> list(String collection, String namespace);
}
