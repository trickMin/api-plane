package com.netease.cloud.nsf;

import com.netease.cloud.nsf.client.ConfigClient;
import com.netease.cloud.nsf.client.ConfigStore;
import com.netease.cloud.nsf.client.MockCofigClient;
import com.netease.cloud.nsf.client.MockConfigStore;
import com.netease.cloud.nsf.service.APIService;
import com.netease.cloud.nsf.service.APIServiceImpl;
import com.netease.cloud.nsf.service.ModelProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/18
 **/
@Configuration
public class APIPlaneAutoConfiguration {

    @Bean
    public ConfigClient configClient() {
        return new MockCofigClient();
    }

    @Bean
    public ConfigStore configStore() {
        return new MockConfigStore();
    }

    @Bean
    public ModelProcessor modelProcessor() {
        return new ModelProcessor();
    }

    @Bean
    public APIService apiService(ModelProcessor processor, ConfigClient client, ConfigStore store) {
        return new APIServiceImpl(processor, client, store);
    }

}
