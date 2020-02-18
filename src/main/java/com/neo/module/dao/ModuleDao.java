package com.neo.module.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.neo.module.bo.ModuleBo;
import com.zaxxer.hikari.HikariDataSource;

import oracle.jdbc.internal.OracleTypes;

@Repository
@Transactional
public class ModuleDao {

	@Autowired
	private HikariDataSource dataSource;

	/**
	 * select All record satisfy the condition state = 1, same module group and
	 * start date <= system date
	 * 
	 * @return
	 */

	public List<ModuleBo> findByModuleGroupAndStartDate(String sql, String moduleGroup, Date startDate) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		List<ModuleBo> moduleBos = new ArrayList<ModuleBo>();
		try {
			connection = dataSource.getConnection();
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, moduleGroup);
			preparedStatement.setDate(2, new java.sql.Date(startDate.getTime()));
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				ModuleBo moduleBo = new ModuleBo();
				moduleBo.setId(resultSet.getLong(1));
				moduleBo.setModuleName(resultSet.getNString(2));
				moduleBo.setIp(resultSet.getNString(3));
				moduleBo.setState(resultSet.getLong(4));
				moduleBo.setPort(resultSet.getLong(5));
				moduleBo.setModuleGroup(resultSet.getNString(6));
				moduleBo.setIsMaster(resultSet.getLong(7));
				moduleBo.setStartDate(resultSet.getDate(8));
				moduleBos.add(moduleBo);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return moduleBos;
	}

	/**
	 * 
	 * @return
	 * 
	 *         get one module by module name module name config in
	 *         application.properties : module.name
	 */
	public ModuleBo findByModuleName(String sql, String moduleName) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		ModuleBo moduleBo = new ModuleBo();
		try {
			connection = dataSource.getConnection();
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, moduleName);
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				moduleBo.setId(resultSet.getLong(1));
				moduleBo.setModuleName(resultSet.getNString(2));
				moduleBo.setIp(resultSet.getNString(3));
				moduleBo.setState(resultSet.getLong(4));
				moduleBo.setPort(resultSet.getLong(5));
				moduleBo.setModuleGroup(resultSet.getNString(6));
				moduleBo.setIsMaster(resultSet.getLong(7));
				moduleBo.setStartDate(resultSet.getDate(8));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		if (moduleBo.getId() != null) {
			return moduleBo;
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param id
	 * 
	 *           update colum isMaster = 1 in DB where id = input and orther colum
	 *           isMaster =0
	 */
	public void updateMaster(String moduleOn, Long idNewMaster, String proc, String modulegroup) {
		Connection connection = null;
		CallableStatement callableStatement = null;
		try {
			connection = dataSource.getConnection();
			callableStatement = connection.prepareCall(proc);
			callableStatement.setLong(1, idNewMaster);
			callableStatement.setString(2, moduleOn);
			callableStatement.setString(3, modulegroup);
			callableStatement.execute();
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(callableStatement!=null) {
				try {
					callableStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}if(connection!=null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * update list module.
	 * 
	 * @param moduleBos
	 */
	public int updateAll(ModuleBo module,List<ModuleBo> moduleBos, String proc, String moduleGroup) {
		Connection connection = null;
		CallableStatement callableStatement = null;
		int k=0;
		try {
			connection = dataSource.getConnection();
			StringBuilder str = new StringBuilder();
			for(ModuleBo moduleBo : moduleBos) {
				str.append(moduleBo.getId());
				str.append(",");
			}
			if(moduleBos.size()==0)
				str.append(",");
			else
				str.delete(str.length()-1, str.length());
			callableStatement = connection.prepareCall(proc);
			callableStatement.setLong(1, module.getId());
			callableStatement.setString(2, str.toString());
			callableStatement.setString(4, moduleGroup);
			callableStatement.registerOutParameter(3, OracleTypes.INTEGER);
			callableStatement.execute();
			k = callableStatement.getInt(3);
			return k;
			
		} catch (Exception e) {
			e.printStackTrace();
			return k;
			
		} finally {
			if(callableStatement !=null) {
				try {
					callableStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(connection!=null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * update module.
	 * 
	 * @param ModuleBo
	 */
	public void updateModule(String sql, ModuleBo moduleBo) {
		//câu sql de update thang module nay co state = 1
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			connection= dataSource.getConnection();
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setLong(1, moduleBo.getId());
			preparedStatement.executeUpdate();
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(preparedStatement!=null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}if(connection!=null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}

	public List<Map<String, String>> getNumberConnectTion(List<ModuleBo> list) {
		
		if(!list.isEmpty()) {
			StringBuilder sql = new StringBuilder("SELECT module, COUNT(1) FROM v$session WHERE 1=1 AND module IN (");
			for(int i=0; i<list.size()-1;i++) {
				sql.append("'"+list.get(i).getModuleName()+"',");
			}
			sql.append("'"+list.get(list.size()-1).getModuleName()+"') GROUP BY module");
			Connection connection = null;
			PreparedStatement preparedStatement = null;
			ResultSet resultSet = null;
			try {
				connection = dataSource.getConnection();
				preparedStatement = connection.prepareStatement(sql.toString());
				resultSet = preparedStatement.executeQuery();
				List<Map<String, String>> listModule = new ArrayList<Map<String,String>>();
				while(resultSet.next()) {
					Map<String, String> map = new HashMap<String, String>();
					map.put(resultSet.getNString(1), resultSet.getNString(2));
					listModule.add(map);
				}
				return listModule;
			}catch (Exception e) {
				e.printStackTrace();
				return new ArrayList<Map<String,String>>();
			}finally {
				if(resultSet!=null) {
					try {
						resultSet.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(preparedStatement!=null) {
					try {
						preparedStatement.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}if(connection!=null) {
					try {
						connection.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		else return new ArrayList<Map<String, String>>();
	}

}
