package com.netease.cloud.nsf.client;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/17
 **/
public class MockCofigClient implements ConfigClient {

    @Override
    public <T> void updateConfig(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteConfig(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getConfig(String id) {
        throw new UnsupportedOperationException();
    }
}
