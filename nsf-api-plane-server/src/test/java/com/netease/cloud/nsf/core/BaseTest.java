package com.netease.cloud.nsf.core;

import com.netease.cloud.nsf.ApiPlaneApplication;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/8/26
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ApiPlaneApplication.class)
@PropertySource("classpath:application.properties")
public class BaseTest {


}
