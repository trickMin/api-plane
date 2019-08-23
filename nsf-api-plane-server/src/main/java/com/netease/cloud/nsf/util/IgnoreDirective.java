package com.netease.cloud.nsf.util;

import freemarker.core.Environment;
import freemarker.template.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/8/23
 **/
public class IgnoreDirective implements TemplateDirectiveModel {

    private static final String IGNORE = "ignore";

    @Override
    public void execute(Environment environment, Map parameters, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {

        String ignore = null;
        final Iterator iterator = parameters.entrySet().iterator();
        while (iterator.hasNext())
        {
            final Map.Entry entry = (Map.Entry) iterator.next();
            final String name = (String) entry.getKey();
            final TemplateModel value = (TemplateModel) entry.getValue();

            if (name.equals(IGNORE))
            {
                ignore = ((SimpleScalar) value).getAsString();
            }
            else
            {
                throw new TemplateModelException("Unsupported parameter '" + name + "'");
            }
        }

        final StringWriter writer = new StringWriter();
        body.render(writer);
        final String string = writer.toString();
        final String lineFeed = "\n";
        final boolean containsLineFeed = string.contains(lineFeed) == true;
        final String[] tokens = string.split(lineFeed);
//        final String p = "^\\s*" + ignore + ".*";

        for (String token : tokens) {
            if (token.contains(ignore + ": ")) {
                token = token.substring(token.indexOf(ignore) + ignore.length() + 2);
            }
            environment.getOut().write(token + (containsLineFeed == true ? lineFeed : ""));
        }

        writer.close();
    }
}
