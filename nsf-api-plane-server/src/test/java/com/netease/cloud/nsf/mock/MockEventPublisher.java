package com.netease.cloud.nsf.mock;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/6/5
 **/
public class MockEventPublisher implements ApplicationEventPublisher {

    @Override
    public void publishEvent(ApplicationEvent event) {

    }

    @Override
    public void publishEvent(Object event) {

    }
}
