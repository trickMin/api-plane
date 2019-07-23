package com.netease.cloud.nsf.client;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/17
 **/
public class MockConfigStore implements ConfigStore {
    @Override
    public <T> void create(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void delete(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void update(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T get(T t) {
        throw new UnsupportedOperationException();
    }
}
