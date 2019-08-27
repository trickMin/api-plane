package com.netease.cloud.nsf.util.freemarker;

import com.netease.cloud.nsf.core.template.TemplateConst;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/8/26
 **/
public class SupplyDirective implements TemplateDirectiveModel {

    enum Keyword {

        MATCH("match:", indent(wrap(TemplateConst.VIRTUAL_SERVICE_MATCH), 4)),
        ROUTE("route:", indent(wrap(TemplateConst.VIRTUAL_SERVICE_ROUTE), 4)),
        EXTRA("extra:", indent(wrap(TemplateConst.VIRTUAL_SERVICE_EXTRA), 4)),
        NAME("name:", indent("name: " + wrap(TemplateConst.VIRTUAL_SERVICE_NAME), 4))
        ;

        String name;
        String replacement;

        Keyword(String name, String replacement) {
            this.name = name;
            this.replacement = replacement;
        }
    }

    private static String indent(String str, int count) {
        return "<@indent count=" + count + ">" + str + "</@indent>";
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
        final String string = writer.toString();
        final String lineFeed = "\n";
        final boolean containsLineFeed = string.contains(lineFeed) == true;
        final String end = containsLineFeed == true ? lineFeed : "";
        final String[] tokens = string.split(lineFeed);

        List<Keyword> keywords = new ArrayList<>(Arrays.asList(Keyword.values()));

        Iterator<Keyword> iterator = keywords.iterator();
        while(iterator.hasNext()) {
            Keyword k = iterator.next();
            for (String token :tokens) {
                if (token.contains(k.name)) {
                    iterator.remove();
                    break;
                }
            }
        }

        for (String token : tokens) {
            environment.getOut().write(token + end);
        }

        for (Keyword keyword : keywords) {
            environment.getOut().write(keyword.replacement + end);
        }
        writer.close();
    }
}
