package com.netease.cloud.nsf.core.plugin;

import com.sun.javafx.binding.StringFormatter;
import org.springframework.stereotype.Component;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/5
 **/
@Component
public class FlowlimitProcessor implements SchemaProcessor {
    @Override
    public String getName() {
        return "FlowlimitProcessor";
    }

    @Override
    public String process(String plugin) {
        return StringFormatter.format("process with %s", this.getClass().getName()).getValue();
    }
}
