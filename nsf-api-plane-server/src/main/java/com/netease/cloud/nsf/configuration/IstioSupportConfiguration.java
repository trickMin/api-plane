package com.netease.cloud.nsf.configuration;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.utils.HttpClientUtils;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static okhttp3.TlsVersion.TLS_1_1;
import static okhttp3.TlsVersion.TLS_1_2;


@Configuration
@ConditionalOnProperty(value = "k8sApiServer")
public class IstioSupportConfiguration {

    @Value("${k8sApiServer}")
    private String k8sApiServer;

    @Value("${certData}")
    private String certData;

    @Value("${keyData}")
    private String keyData;

    @Bean
    public Config config() {
        Config config = new ConfigBuilder()
                .withMasterUrl(k8sApiServer)
                .withTrustCerts(true)
                .withDisableHostnameVerification(true)
                .withClientCertData(certData)
                .withClientKeyData(keyData)
                .withClientKeyPassphrase("passphrase")
                .withWatchReconnectInterval(5000)
                .withWatchReconnectLimit(5)
                .withRequestTimeout(5000)
                .withTlsVersions(TLS_1_2, TLS_1_1)
                .build();
        return config;
    }

    @Bean
    public OkHttpClient httpClient(Config config) {
        return HttpClientUtils.createHttpClient(config);
    }
}

