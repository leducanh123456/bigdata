package com.neo.config;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;

@Configuration
@Order(100)
public class PropertyConfig {

	@Bean("propertiesConfig")
	@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
	public PropertiesConfiguration fileInfoPropertiesConfig() throws ConfigurationException {
		PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
		propertiesConfiguration.setDelimiterParsingDisabled(true);
		propertiesConfiguration.setEncoding("UTF8");
		propertiesConfiguration.setPath("config/job.properties");
		propertiesConfiguration.load();
		propertiesConfiguration.setReloadingStrategy(new FileChangedReloadingStrategy());
		return propertiesConfiguration;
	}
	
	@Bean("propertiesSql")
	@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
	public PropertiesConfiguration fileInfoPropertiesSqlConfig() throws ConfigurationException {
		PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
		propertiesConfiguration.setDelimiterParsingDisabled(true);
		propertiesConfiguration.setEncoding("UTF8");
		propertiesConfiguration.setPath("config/sql.properties");
		propertiesConfiguration.load();
		propertiesConfiguration.setReloadingStrategy(new FileChangedReloadingStrategy());
		return propertiesConfiguration;
	}
}
