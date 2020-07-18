package com.yc.community.community;

import com.yc.community.CommunityApplication;
import com.yc.community.util.CommunityUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunityApplication.class)
public class TestWk {
    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Test
    public void testWk() {
        String cmd = wkImageCommand + " --quality 75" + " http://localhost:8080/community/index " +
                wkImageStorage + "/" + CommunityUtil.generateUUID() + ".png";
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
