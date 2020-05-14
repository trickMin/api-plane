package com.netease.cloud.nsf.mcp.status;

import com.netease.cloud.nsf.mcp.dao.StatusDao;
import com.netease.cloud.nsf.mcp.dao.meta.Status;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/5/6
 **/
public class StatusNotifierImpl implements StatusNotifier {
    private ValueGenerator defaultGenerator;

    private StatusDao statusDao;

    public StatusNotifierImpl(StatusDao statusDao, ValueGenerator defaultGenerator) {
        this.statusDao = statusDao;
        this.defaultGenerator = defaultGenerator;
    }

    @Override
    public void notifyStatus(String key) {
        statusDao.update(new Status(key, defaultGenerator.generate(key)));
    }

    @Override
    public void notifyStatus(String key, String value) {
        statusDao.update(new Status(key, value));
    }

    @Override
    public void notifyStatus(String key, ValueGenerator valueGenerator) {
        statusDao.update(new Status(key, valueGenerator.generate(key)));
    }
}
