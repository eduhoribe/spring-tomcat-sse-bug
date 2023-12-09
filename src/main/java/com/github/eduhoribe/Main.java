package com.github.eduhoribe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class Main extends SpringBootServletInitializer {

	public static void main(String[] args) {
		new SpringApplication(Main.class).run(args);
	}
}