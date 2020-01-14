package com.netease.cloud.nsf.config;

import com.netease.cloud.nsf.util.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author zhangzihao
 */
@Component
public class DaemonSetConfig {

    private String defaultSidecarFilPath = "/home/envoy";

    @Autowired
    private Environment environment;


    public String getSidecarFilePath(){
        String currentPath = environment.getProperty("sidecarPath");
        if (StringUtils.isEmpty(currentPath)){
            return defaultSidecarFilPath;
        }
        return currentPath;
    }

    public String getNsfMetaUrl(){
        return environment.getProperty("nsfMetaUrl");
    }

    public int getDownloadRetryWait(){
        String waitTime = environment.getProperty("waitTime");
        if (!StringUtils.isEmpty(waitTime)){
            return Integer.parseInt(waitTime);
        }
        return Const.DEFAULT_WAIT_TIME;
    }

    public int getReTryCount(){
        int result;
        String reTry = environment.getProperty("reTry");
        if (!StringUtils.isEmpty(reTry)){
            result =  Integer.parseInt(reTry);
            // 最少去下载一次
            if (result <= 0){
                return 1;
            }else {
                return result;
            }
        }
        return Const.DEFAULT_RETRY_COUNT;
    }

}
