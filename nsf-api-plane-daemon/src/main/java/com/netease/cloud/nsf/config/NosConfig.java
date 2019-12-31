package com.netease.cloud.nsf.config;

import com.netease.cloud.auth.BasicCredentials;
import com.netease.cloud.auth.Credentials;
import com.netease.cloud.nsf.util.Const;
import com.netease.cloud.services.nos.NosClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@Configuration
public class NosConfig {

    private String nosSecretKey;
    private String nosAccessKey;
    private String nosBucketName;
    private String nosEndPoint;
    private String nosFilePath;


    @Autowired
    private Environment environment;

    @Bean
    public NosClient nosClient(){

        nosSecretKey = environment.getProperty("nosSecretKey");
        if (StringUtils.isEmpty(nosAccessKey)){
            nosSecretKey = Const.DEFAULT_NOS_SECRET_KEY;
        }
        nosAccessKey = environment.getProperty("nosAccessKey");
        if (StringUtils.isEmpty(nosAccessKey)){
            nosAccessKey = Const.DEFAULT_NOS_ACCESS_KEY;
        }
        nosBucketName = environment.getProperty("nosBucketName");
        if (StringUtils.isEmpty(nosBucketName)){
            nosBucketName = Const.DEFAULT_NOS_BUCKETNAME;
        }
        nosEndPoint = environment.getProperty("nosEndPoint");
        if (StringUtils.isEmpty(nosEndPoint)){
            nosEndPoint = Const.DEFAULT_NOS_NOSENDPOINT;
        }
        nosFilePath = environment.getProperty("nosFilePath");
        if (StringUtils.isEmpty(nosFilePath)){
            nosFilePath = Const.DEFAULT_NOS_NOS_FILE_PATH;
        }
        Credentials credentials = new BasicCredentials(nosAccessKey, nosSecretKey);
        NosClient nosClient = new NosClient(credentials);
        nosClient.setEndpoint(nosEndPoint);
        return nosClient;
    }

    public String getNosSecretKey() {
        return nosSecretKey;
    }

    public void setNosSecretKey(String nosSecretKey) {
        this.nosSecretKey = nosSecretKey;
    }

    public String getNosAccessKey() {
        return nosAccessKey;
    }

    public void setNosAccessKey(String nosAccessKey) {
        this.nosAccessKey = nosAccessKey;
    }

    public String getNosBucketName() {
        return nosBucketName;
    }

    public void setNosBucketName(String nosBucketName) {
        this.nosBucketName = nosBucketName;
    }

    public String getNosEndPoint() {
        return nosEndPoint;
    }

    public void setNosEndPoint(String nosEndPoint) {
        this.nosEndPoint = nosEndPoint;
    }

    public String getNosFilePath() {
        return nosFilePath;
    }

    public void setNosFilePath(String nosFilePath) {
        this.nosFilePath = nosFilePath;
    }
}
