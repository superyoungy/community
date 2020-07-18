package com.yc.community.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * 好像没用，待考证
 */
@Configuration
public class SpringUtil {
    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext applicationContext) {
        SpringUtil.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
