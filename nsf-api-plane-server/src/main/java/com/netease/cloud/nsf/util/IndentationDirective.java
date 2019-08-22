package com.netease.cloud.nsf.util;

import freemarker.core.Environment;
import freemarker.template.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/8/9
 **/
public class IndentationDirective implements TemplateDirectiveModel {

    private static final String COUNT = "count";
    private static final String FIRST_LINE = "firstLine";

    @Override
    public void execute(Environment environment, Map parameters, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {

        Integer count = null;
        Integer firstLine = null;
        final Iterator iterator = parameters.entrySet().iterator();
        while (iterator.hasNext())
        {
            final Map.Entry entry = (Map.Entry) iterator.next();
            final String name = (String) entry.getKey();
            final TemplateModel value = (TemplateModel) entry.getValue();

            if (name.equals(COUNT))
            {
                if (value instanceof TemplateNumberModel == false)
                {
                    throw new TemplateModelException("The \"" + COUNT + "\" parameter " + "must be a number");
                }
                count = ((TemplateNumberModel) value).getAsNumber().intValue();
                if (count < 0)
                {
                    throw new TemplateModelException("The \"" + COUNT + "\" parameter " + "cannot be negative");
                }
            }
            else if (name.equals(FIRST_LINE))
            {
                if (value instanceof TemplateNumberModel == false)
                {
                    throw new TemplateModelException("The \"" + FIRST_LINE + "\" parameter " + "must be a number");
                }
                firstLine = ((TemplateNumberModel) value).getAsNumber().intValue();
                if (firstLine < 0)
                {
                    throw new TemplateModelException("The \"" + FIRST_LINE + "\" parameter " + "cannot be negative");
                }
            }
            else
            {
                throw new TemplateModelException("Unsupported parameter '" + name + "'");
            }
        }
        if (count == null)
        {
            throw new TemplateModelException("The required \"" + COUNT + "\" parameter" + "is missing");
        }

        final String indentation = StringUtils.repeat(' ', count);
        final StringWriter writer = new StringWriter();
        body.render(writer);
        final String string = writer.toString();
        final String lineFeed = "\n";
        final boolean containsLineFeed = string.contains(lineFeed) == true;
        final String[] tokens = string.split(lineFeed);

        for (int i = 0; i < tokens.length; i++) {
            String indent = indentation;
            if (i == 0 && firstLine != null) {
                indent = StringUtils.repeat(' ', firstLine);
            }
            environment.getOut().write(indent + tokens[i] + (containsLineFeed == true ? lineFeed : ""));
        }
        writer.close();
    }
}
