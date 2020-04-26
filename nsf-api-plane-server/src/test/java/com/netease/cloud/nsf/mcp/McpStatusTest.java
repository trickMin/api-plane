package com.netease.cloud.nsf.mcp;

import com.netease.cloud.nsf.mcp.status.Status;
import org.junit.Test;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/23
 **/
public class McpStatusTest {

    @Test
    public void test() {
        Status s1 = new Status(new Status.Property[]{
                new Status.Property("b", "1"), new Status.Property("f", "2")
        });
        Status s2 = new Status(new Status.Property[]{
                new Status.Property("f", "2")
        });
        Status s3 = new Status(new Status.Property[]{
                new Status.Property("f", "2"), new Status.Property("c", "4"), new Status.Property("b", "5")
        });

        Status s4 = new Status(new Status.Property[]{
                new Status.Property("b", "5"), new Status.Property("d", "9"), new Status.Property("f", "10"), new Status.Property("j", "11")
        });
        Status s5 = new Status(new Status.Property[0]);

        Status.Difference d1 = s1.compare(s3);
        Status.Difference d2 = s3.compare(s1);
        Status.Difference d3 = s4.compare(s3);
        Status.Difference d4 = s3.compare(s4);
        Status.Difference d5 = s5.compare(s4);
        Status.Difference d6 = s4.compare(s5);
    }
}
