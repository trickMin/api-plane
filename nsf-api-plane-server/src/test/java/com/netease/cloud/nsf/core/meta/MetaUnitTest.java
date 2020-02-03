package com.netease.cloud.nsf.core.meta;
import com.netease.cloud.nsf.core.BaseTest;
import org.junit.Test;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @description:
 * @author: zhangzihao1@corp.netease.com
 * @create: 2018-12-22
 **/
public class MetaUnitTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(MetaUnitTest.class);

    @Test
    public void testForAllDaoMeta() {
        logger.info("MetaUnitTest ==== start ====");
        Configuration configuration = new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage("com.netease.cloud.nsf.meta"))
                .setScanners(new SubTypesScanner(false));
        Reflections reflections = new Reflections(configuration);
        Set<Class<?>> allClass = reflections.getSubTypesOf(Object.class);
        for (Class<?> clazz : allClass) {
            try {
                ClassMethodExecutor executor = new ClassMethodExecutor(clazz);
                executor.executeAllMethod();
            }catch (Exception e){
                continue;
            }
        }
        logger.info("MetaUnitTest ==== end ====");

    }


}
