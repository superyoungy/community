package com.yc.community.community;

import com.yc.community.CommunityApplication;
import com.yc.community.util.SpringUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunityApplication.class)
public class CommunityApplicationTests implements ApplicationContextAware {
//	ApplicationContext context=null;
	private static ApplicationContext context = null;
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context=applicationContext;
	}

	@Test
	public void testApplicationContext() {
		System.out.println(CommunityApplicationTests.context);
		System.out.println(CommunityApplicationTests.context.getBean("userService"));
	}

	@Test
	public void testApplicationTest2() {
		ApplicationContext applicationContext = SpringUtil.getApplicationContext();
		System.out.println(applicationContext.getBean("userService"));
	}

	@Test
	public void bitMove () {
		System.out.println(1 << 2);
	}

	@Test
	public void testMap() {
		Map<String, Object> map = new HashMap<>();
		System.out.println(map.get("ggg"));
	}

	@Test
	public void testInstance() {
		HashMap<String, Object> map = new HashMap<>();
		System.out.println(map == null);
	}
}
