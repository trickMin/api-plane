package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/8
 **/
public class CircuitBreakerProcessorTest extends BasePluginTest {
    @Autowired
    CircuitBreakerProcessor processor;

    @Test
    public void process() {
        String p1 = "{\n" +
                "  \"kind\": \"circuit-breaker\",\n" +
                "  \"config\": {\n" +
                "    \"consecutive_slow_requests\": 3,\n" +
                "    \"average_response_time\": \"0.1s\",\n" +
                "    \"min_request_amount\": 3,\n" +
                "    \"error_percent_threshold\": 50,\n" +
                "    \"break_duration\": \"50s\",\n" +
                "    \"lookback_duration\": \"10s\"\n" +
                "  },\n" +
                "  \"response\": {\n" +
                "    \"code\": \"200\",\n" +
                "    \"body\": \"{\\\"ba\\\":\\\"ba\\\"}\",\n" +
                "    \"headers\": [\n" +
                "      {\n" +
                "        \"key\": \"buhao\",\n" +
                "        \"value\": \"buhaoyabuhao\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        FragmentHolder fragment1 = processor.process(p1, serviceInfo);
    }
}
