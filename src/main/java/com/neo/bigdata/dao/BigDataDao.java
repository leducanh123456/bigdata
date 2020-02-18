package com.neo.bigdata.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.neo.utils.ExtractException;
import com.zaxxer.hikari.HikariDataSource;

@Repository
@Transactional
public class BigDataDao {

	@Autowired
	private HikariDataSource ds;

	private final Logger logger = LoggerFactory.getLogger(BigDataDao.class);

	public BigDataDao() {

	}

	public List<Object> listBigData(String sql) {
		List<Object> list = new ArrayList<>();
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection = ds.getConnection();
			preparedStatement = connection.prepareStatement(sql);
			resultSet = preparedStatement.executeQuery();
			int col = resultSet.getMetaData().getColumnCount();
			while (resultSet.next()) {
				String s = "";
				for (int i = 1; i <= col; i++) {
					s += resultSet.getObject(i).toString() + "    ";
				}
				list.add(s);
			}

		} catch (Exception e) {
			logger.info("exception BigDataDao {}", ExtractException.exceptionToString(e));
		}
		return list;
	}

}
