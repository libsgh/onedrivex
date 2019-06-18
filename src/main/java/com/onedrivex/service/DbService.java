package com.onedrivex.service;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.druid.pool.DruidDataSource;

import cn.hutool.db.Db;

@Service
public class DbService {
	
	private final static Logger logger = LoggerFactory.getLogger(DbService.class);
	
	@Autowired
	private DruidDataSource ds;
	
	/**
	 * 批量执行sql
	 * @param sqls
	 * @return
	 */
	public int execBatch(List<String> sqls) {
		try {
			String[] sqlArray = new String[sqls.size()];
			sqls.toArray(sqlArray);
			int[] rows = Db.use(ds).executeBatch(sqlArray);
			int count = Arrays.stream(rows).sum();
			return count;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return 0;
	}
	
	/*
	 * 根据key获取配置信息
	 */
	public String getConfig(String key) {
		String value = "";
		try {
			value = Db.use(ds).queryString("select value from config where key=?", key);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return value;
	}

	/**
	 * 更新配置
	 * @param key
	 * @param value
	 */
	public void updateConfig(String key, String value) {
		try {
			Db.use(ds).execute("update config set value=? where key=?", value, key);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
}
