package com.yc.community.community;

import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@SpringBootTest
//@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests implements ApplicationContextAware {
	ApplicationContext context=null;
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context=applicationContext;
	}
}
