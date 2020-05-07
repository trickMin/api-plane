package com.netease.cloud.nsf.mcp.status;

import com.netease.cloud.nsf.mcp.dao.StatusDao;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/23
 **/
public class StatusProductorImpl implements StatusProductor {
    private StatusDao dao;

    public StatusProductorImpl(StatusDao dao) {
        this.dao = dao;
    }

    @Override
    public Status product() {
        List<com.netease.cloud.nsf.mcp.dao.meta.Status> statuses = dao.list();
        List<Status.Property> properties = new ArrayList<>();
        statuses.forEach(item -> {
            properties.add(new Status.Property(item.getName(), item.getValue()));
        });
        return new Status(properties.toArray(new Status.Property[0]));
    }
}
