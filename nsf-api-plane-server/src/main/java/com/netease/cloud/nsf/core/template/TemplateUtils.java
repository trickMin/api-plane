package com.netease.cloud.nsf.core.template;

import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;


/**
 * Template工具类
 * 1. 可以将一个Template拆分成多个TemplateWrapper
 * 2. 可以根据Label查找TemplateWrapper
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/1
 **/
public class TemplateUtils {

    private static final String YAML_SPLIT = "---";

    private TemplateUtils() {
    }

    public static List<TemplateWrapper> getWrappersWithFilter(Template template, Predicate<TemplateWrapper> filter) {
        List<TemplateWrapper> wrappers = getSpiltWrapper(template);
        List<TemplateWrapper> ret = new ArrayList<>();
        wrappers.forEach(wrapper -> {
            if (filter.test(wrapper))
                ret.add(wrapper);
        });
        return ret;
    }

    public static TemplateWrapper getWrapperWithFilter(Template template, Predicate<TemplateWrapper> filter) {
        List<TemplateWrapper> wrappers = getSpiltWrapper(template);
        for (TemplateWrapper wrapper : wrappers) {
            if (filter.test(wrapper)) {
                return wrapper;
            }
        }
        return null;
    }

    public static List<TemplateWrapper> getSpiltWrapper(Template template) {
        List<TemplateWrapper> wrappers = new ArrayList<>();
        List<String> schemes = spilt(template.toString());
        for (String scheme : schemes) {
            wrappers.add(getWrapper(template.getName(), scheme, template.getConfiguration()));
        }
        return wrappers;
    }

    public static TemplateWrapper getWrapper(Template template) {
        try {
            return new TemplateWrapper(template);
        } catch (IOException e) {
            throw new ApiPlaneException("Wrapper template Failure", e);
        }
    }

    public static TemplateWrapper getWrapper(String name, String source, Configuration configuration) {
        try {
            return new TemplateWrapper(name, source, configuration);
        } catch (IOException e) {
            throw new ApiPlaneException("Wrapper template Failure", e);
        }
    }

    public static Template getTemplate(String name, Configuration configuration) {
        try {
            return configuration.getTemplate(name);
        } catch (IOException e) {
            throw new ApiPlaneException("Get template Failure", e);
        }
    }

    /**
     * 切分context，并且过滤掉空行和非代码scheme
     */
    public static List<String> spilt(String context) {
        List<String> ret = new ArrayList<>();
        // 过滤空行
        context = filter(context, BLANK_LINE);
        String[] schemes = context.split(YAML_SPLIT);

        for (String scheme : schemes) {
            if (contain(scheme, IGNORE_SCHEME) && !"".equals(scheme)) {
                ret.add(scheme);
            }
        }
        return ret;
    }

    private static String filter(String templateSource, String... filters) {
        String tmp = templateSource;
        for (String filter : filters) {
            tmp = Pattern.compile(filter).matcher(tmp).replaceAll("");
        }
        return tmp;
    }

    private static boolean contain(String source, String regex) {
        return Pattern.compile(regex).matcher(source).find();
    }
}
