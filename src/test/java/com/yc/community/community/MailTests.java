package com.yc.community.community;

import com.yc.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
@RunWith(SpringRunner.class)//必须用该类进行测试，普通的junit不支持html方式的邮件发送
@SpringBootTest
//@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void mailTest() {
        String to = "superyoungy@sina.com";
        String subject="SpringBootStarterMailTest";
        String content="Hello!";
        mailClient.sendMail(to,subject,content);
    }

    @Test
    public void testHtmlMail() {
        Context context = new Context();
        context.setVariable("username", "superyoungy(sina)");

        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        mailClient.sendMail("superyoungy@sina.com", "HTML", content);
    }
}
