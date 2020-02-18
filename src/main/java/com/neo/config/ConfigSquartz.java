package com.neo.config;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.neo.bigdata.service.BigDataService;
import com.neo.module.bo.ModuleBo;
import com.neo.monitor.NEOMonitorCluster;
import com.neo.squartz.BigData;

@Configuration
public class ConfigSquartz {
	
	@Autowired
	private BigDataService bigDataService;
	
	SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
	
	private Map<String, Object> map = new HashMap<String, Object>();
	
	@Bean(name = "schedulerFactory")
	public SchedulerFactoryBean schedulerFactoryBean() {
		scheduler.setTriggers(
				
		);
		return scheduler;
		
	}
	
	@Bean("monitor")
	@Scope("singleton")
	public NEOMonitorCluster getCluster() {
		return new NEOMonitorCluster();
	}
	@Bean("listModule")
	@Scope("singleton")
	public List<ModuleBo> getListJob() {
		List<ModuleBo> jobs = Collections.synchronizedList(new ArrayList<ModuleBo>());
		return jobs;
	}
	@Autowired
	@Qualifier("propertiesConfig")
	private PropertiesConfiguration pro;
	
	@Bean("mapJobSocket")
	@Scope("singleton")
	public ConcurrentHashMap<ModuleBo, SocketChannel> getMapJobSocket() {
		return new ConcurrentHashMap<ModuleBo, SocketChannel>();
	}
	@Bean("retry")
	@Scope("singleton")
	public ConcurrentHashMap<ModuleBo, SocketChannel> getConcurrentHashMap() {
		return new ConcurrentHashMap<ModuleBo, SocketChannel>();
	}
	
	@Bean("thisModule")
	@Scope("singleton")
	public ModuleBo getJobBo() {
		return new ModuleBo();
	}
	
	@Bean(name = "jobBigData")
	public JobDetailFactoryBean jobDetailFactoryBeanBigData() {
		JobDetailFactoryBean factory = new JobDetailFactoryBean();
		factory.setJobClass(BigData.class);
		map.put("bigDataService", bigDataService);
		map.put("pro", pro);
		factory.setJobDataAsMap(map);
		factory.setGroup("bigData");
		factory.setName("bigData");
		return factory;
	}

	@Bean(name = "bigData")
	public CronTriggerFactoryBean cronTriggerFactoryBeanBigData() {
		CronTriggerFactoryBean stFactory = new CronTriggerFactoryBean();
		stFactory.setJobDetail(jobDetailFactoryBeanBigData().getObject());
		stFactory.setName("bigData");
		stFactory.setGroup("bigData");
		stFactory.setCronExpression(pro.getString("big.data.scheduler").trim());
		return stFactory;
	}
	
}
