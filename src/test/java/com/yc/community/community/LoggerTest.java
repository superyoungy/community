package com.yc.community.community;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LoggerTest {
    private static final Logger logger= LoggerFactory.getLogger(LoggerTest.class);

    @Test
    public void testLogger() {
        System.out.println(logger.getName());
        logger.info("info");
        logger.warn("warn");
        logger.debug("debug");
        logger.error("error");

    }
}
