package com.netease.cloud.nsf.core.template;

/**
 * 支持TemplateWrapper的regex expression
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/2
 **/
public class TemplateConst {
    public static final String DESCRIPTION_TAG = "(?m)^#(?!@)(.*)$";
    public static final String LABEL_TAG = "(?m)^#@(.*)=(.*)$";
    public static final String BLANK_LINE = "(?m)^\\s*$(?:\\n|\\r\\n)";
    public static final String IGNORE_SCHEME = "(?m)^(?!#)(.*)$";
}
