package com.neo.bigdata.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.neo.bigdata.dao.BigDataDao;

@Service
public class BigDataService {
	@Autowired
	private BigDataDao bigDataDao;

	public List<Object> listBigData(String sql) {
		return bigDataDao.listBigData(sql);
	}
}
