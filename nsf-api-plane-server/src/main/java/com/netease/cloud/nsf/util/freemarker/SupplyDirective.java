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
        EXTRA("extra: ", TemplateConst.VIRTUAL_SERVICE_EXTRA),
        MATCH("match: ", TemplateConst.VIRTUAL_SERVICE_MATCH),
        ROUTE("route: ", TemplateConst.VIRTUAL_SERVICE_ROUTE),

        // PUT IT IN THE END. DO NOT MODIFY THE SEQUENCE
        API("api: ", TemplateConst.VIRTUAL_SERVICE_API),
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

    private static String decorate(String str, int count) {
        return indent(wrap(str), count);
    }

    @Override
    public void execute(Environment environment, Map parameters, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {

        final StringWriter writer = new StringWriter();
        if (body != null) {
            body.render(writer);
        }
        final String string = writer.toString();
        final String lineFeed = "\n";
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

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < tokens.length; i++) {
            buffer.append(tokens[i] + lineFeed);
        }

        for (int i = 0; i < keywords.size(); i++) {
            int count = 4;
            if (i == keywords.size() - 1) count = 2;
            buffer.insert(0, decorate(keywords.get(i).replacement, count) + lineFeed);
        }

        environment.getOut().write(buffer.toString());
        writer.close();
    }
}
