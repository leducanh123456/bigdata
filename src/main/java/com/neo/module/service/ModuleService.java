package com.neo.module.service;

import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.neo.module.bo.ModuleBo;
import com.neo.module.dao.ModuleDao;;

@Service
public class ModuleService {

	@Autowired
	private ModuleDao moduleDao;
	
	@Autowired
	@Qualifier("propertiesSql")
	private PropertiesConfiguration prosql;
	
	@Autowired
	@Qualifier("propertiesConfig")
	private PropertiesConfiguration pro;

	/**
	 * select All record satisfy the condition state = 1, same module group and
	 * start date <= system date
	 * 
	 * @return
	 */
	public List<ModuleBo> getAllModule() {
		String moduleGroup = pro.getString("module.group");
		String sql =prosql.getString("sub.sql.get.all.module");
		return moduleDao.findByModuleGroupAndStartDate(sql,moduleGroup, new Date());
	}

	/**
	 * @return get one module by module name module name config in
	 *         application.properties : module.name
	 */
	public ModuleBo getModule(String moduleName) {
		String sql = prosql.getString("sub.sql.find.module.by.module.name");
		return moduleDao.findByModuleName(sql,moduleName);
	}

	/**
	 * @param id update colum isMaster = 1 in DB where id = input and orther colum
	 *           isMaster =0
	 */
	public void updateMaster(ConcurrentHashMap<ModuleBo, SocketChannel> modules, ModuleBo moduleBo) {
		
		StringBuilder str = new StringBuilder();
		for (Map.Entry<ModuleBo, SocketChannel> entryMap : modules.entrySet()) {
			str.append(entryMap.getKey().getId());
			str.append(",");
		}
		str.append(moduleBo.getId());
		String proc = prosql.getString("sub.sql.update.module.master");
		String moduleGroup = pro.getString("module.group");
		moduleDao.updateMaster(str.toString(),moduleBo.getId(),proc,moduleGroup);
	}

	/**
	 * update list module batch
	 * 
	 * @param moduleBos
	 */
	public int updateAll(ModuleBo module,List<ModuleBo> moduleBos) {

		String sql = prosql.getString("sub.sql.update.module.disconnect");
		String moduleGroup = pro.getString("module.group");
		return moduleDao.updateAll(module,moduleBos, sql,moduleGroup);

	}
	
	/**
	 * update  module.
	 * 
	 * @param ModuleBo
	 */
	public void updateModule(ModuleBo moduleBo) {
		String sql= prosql.getString("sub.sql.update.state.active");
		moduleDao.updateModule(sql,moduleBo);
		
	}
	public List<Map<String, String>> getNumberConnectTion(List<ModuleBo> list) {
		return moduleDao.getNumberConnectTion(list);
	}
}
