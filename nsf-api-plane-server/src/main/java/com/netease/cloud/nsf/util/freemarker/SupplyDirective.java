package com.netease.cloud.nsf.util.freemarker;

import com.jayway.jsonpath.Criteria;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.template.TemplateConst;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/8/26
 **/
public class SupplyDirective implements TemplateDirectiveModel {

    enum Keyword {

        MATCH("match:", indent(wrap(TemplateConst.VIRTUAL_SERVICE_MATCH_YAML))),
        ROUTE("route:", indent(wrap(TemplateConst.VIRTUAL_SERVICE_ROUTE_YAML))),
        EXTRA("extra:", indent(wrap(TemplateConst.VIRTUAL_SERVICE_EXTRA_YAML))),
        API("api:", wrap(TemplateConst.API_NAME)),


        ;

        String name;
        String replacement;

        Keyword(String name, String replacement) {
            this.name = name;
            this.replacement = replacement;
        }
    }

    private static String indent(String str) {
        return "<@indent>" + str + "</@indent>";
    }

    private static String wrap(String str) {
        return "${" + str + "}";
    }

    @Override
    public void execute(Environment environment, Map parameters, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        final StringWriter writer = new StringWriter();
        if (body != null) {
            body.render(writer);
        }
        String string = writer.toString();
        if (StringUtils.isEmpty(string)) {
            string = "[{}]";
        }
        ResourceGenerator gen = ResourceGenerator.newInstance(string, ResourceType.YAML);
        gen.createOrUpdateValue("$[?]", "nsf-template-match", Keyword.MATCH.replacement, Criteria.where("match").exists(false));
        gen.createOrUpdateValue("$[?]", "nsf-template-route", Keyword.ROUTE.replacement, Criteria.where("route").exists(false));
        gen.createOrUpdateValue("$[?]", "nsf-template-extra", Keyword.EXTRA.replacement, Criteria.where("extra").exists(false));
        gen.createOrUpdateValue("$[?]", "api", Keyword.API.replacement, Criteria.where("api").exists(false));

        String yaml = gen.yamlString();
        yaml = yaml.replaceAll("(?m)^(-?\\s*)nsf-template-.*?:(?:\\s*)(<.*>)", "$1$2");

        environment.getOut().write(yaml);
        writer.close();
    }
}
