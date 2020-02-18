package com.neo;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.neo.monitor.NEOMonitorCluster;
import com.neo.utils.ExtractException;

@SpringBootApplication
@EnableScheduling
public class App {
	private static final Logger logger = LoggerFactory.getLogger(App.class);
	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(App.class, args);
		NEOMonitorCluster neoMonitorCluster = context.getBean(com.neo.monitor.NEOMonitorCluster.class);
		System.out.println("Server Started");
		try {
			neoMonitorCluster.run();
		} catch (IOException e) {
			logger.error("neoMonitorCluster Exception=" + ExtractException.exceptionToString(e));
		}
	}
}
