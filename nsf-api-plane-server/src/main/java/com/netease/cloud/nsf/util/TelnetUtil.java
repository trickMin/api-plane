package com.netease.cloud.nsf.util;

import io.fabric8.openshift.api.model.PolicyRuleBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2021/9/22
 */
public class TelnetUtil {

    private static final Logger logger = LoggerFactory.getLogger(TelnetUtil.class);


    private static TelnetClient build(String ip, Integer port) throws IOException {
        TelnetClient telnetClient = new TelnetClient();
        telnetClient.connect(ip, port);
        return telnetClient;
    }

    private static void destroy(TelnetClient telnetClient) throws IOException {
        telnetClient.disconnect();
    }

    private static void write(OutputStream out, String command) throws IOException {
        PrintStream print = new PrintStream(out);
        print.println(command);
        print.flush();
       // print.close();
    }


    private static void read(InputStream in, StringBuffer buffer) throws IOException {
        char ch = (char) in.read();
        buffer.append(ch);
    }

    /**
     * 发起telnet命令
     *
     * @param ip
     * @param port
     * @param command 命令
     * @param pattern 结束语
     * @return
     */
    public static String sendCommand(String ip, Integer port, String command, String pattern) {
        StringBuffer buffer = new StringBuffer();
        try {
            logger.info("telent info , ip = {} ,port = {}", ip, port);
            TelnetClient telnetClient = build(ip, port);
            write(telnetClient.getOutputStream(), command);
            while (telnetClient.isAvailable()) {
                read(telnetClient.getInputStream(), buffer);
                if (buffer.toString().endsWith(pattern)) {
                    break;
                }
            }
            destroy(telnetClient);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String info = buffer.toString().replace(pattern, StringUtils.EMPTY);
        logger.info("Telnet info is {}",info);
        return info;
    }

}
