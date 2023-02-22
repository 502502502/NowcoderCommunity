package com.ningct.nowcodercommunity;

import com.ningct.nowcodercommunity.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;

@SpringBootTest
public class MailTest {
    @Resource
    private MailClient mailClient;
    @Resource
    private TemplateEngine templateEngine;

    @Test
    public void sentMessage(){
        mailClient.sentMail("2640069987@qq.com", "test", "hello");
    }
    @Test
    public void sentHtml(){
        Context context = new Context();
        context.setVariable("username", "tom");
        String content = templateEngine.process("/mail/demo", context);
        mailClient.sentMail("2640069987@qq.com", "testHtml", content);
    }
}
