package com.yc.community.community;

import com.yc.community.CommunityApplication;
import com.yc.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveFilterTest {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void init() throws IllegalAccessException {
        sensitiveFilter.method();
    }

    @Test
    public void filter() {
        String s = sensitiveFilter.doFilter("好的123,赌博博吗123开票456赌");
        System.out.println(s);

        String ss = sensitiveFilter.doFilter("这里可以☆赌☆博☆,可以☆嫖☆娼☆,可以☆吸☆毒☆,可以☆开☆票☆,哈哈哈!");
        System.out.println(ss);
    }
}
