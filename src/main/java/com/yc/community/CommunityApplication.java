package com.yc.community;

import com.yc.community.util.SpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {

	@PostConstruct
	public void init() {
		//解决netty启动冲突问题
		//see NettyUtils.serAvailableProcessors()
		System.setProperty("es.set.netty.runtime.available.processors", "false");
	}

	public static void main(String[] args) {
		ApplicationContext applicationContext = SpringApplication.run(CommunityApplication.class, args);
		SpringUtil.setApplicationContext(applicationContext);
	}

}
