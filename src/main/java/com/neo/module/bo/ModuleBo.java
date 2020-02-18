package com.neo.module.bo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity( name = "MODULE")
@Table( name = "MODULE" ,schema  ="vas_platform")

/**
 * 
 * @author DUCANH
 *
 */
public class ModuleBo {
	
	@Id
	private Long id;
	
	@Column(name = "MODULE_NAME", unique = true)
	private String moduleName;
	
	@Column(name = "IP")
	private String ip;
	
	@Column(name = "STATE")
	private Long  state;
	
	@Column(name = "PORT")
	private Long port;
	
	@Column(name = "MODULE_GROUP")
	private String  moduleGroup;
	
	@Column(name = "IS_MASTER")
	private Long isMaster;
	
	@Column(name = "START_DATE")
	private Date startDate;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Long getState() {
		return state;
	}

	public void setState(Long state) {
		this.state = state;
	}

	public Long getPort() {
		return port;
	}

	public void setPort(Long port) {
		this.port = port;
	}

	public String getModuleGroup() {
		return moduleGroup;
	}

	public void setModuleGroup(String moduleGroup) {
		this.moduleGroup = moduleGroup;
	}

	public Long getIsMaster() {
		return isMaster;
	}

	public void setIsMaster(Long isMaster) {
		this.isMaster = isMaster;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	
}
