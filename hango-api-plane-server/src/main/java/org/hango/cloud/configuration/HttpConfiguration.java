package org.hango.cloud.configuration;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/26 15:16
 **/
@Configuration
public class HttpConfiguration {

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient().newBuilder().retryOnConnectionFailure(true).connectionPool(pool())
                .connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).writeTimeout(5, TimeUnit.SECONDS)
                .build();
    }

    @Bean
    public ConnectionPool pool() {
        return new ConnectionPool(50, 5, TimeUnit.MINUTES);
    }
}

