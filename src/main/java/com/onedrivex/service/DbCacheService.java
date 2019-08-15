package com.onedrivex.service;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.druid.pool.DruidDataSource;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.json.JSONUtil;

@Service
public class DbCacheService {
	
	private final static Logger logger = LoggerFactory.getLogger(DbCacheService.class);
	
	@Resource(name = "cache")
	private DruidDataSource cds;
	
	/**
	 * 根据key获取缓存单个数据
	 * @param key
	 * @param resultType
	 * @return
	 */
	public <T> T getOneByKey(String key, Class<T> resultType) {
		try {
			String value =  Db.use(cds).queryString("select value from cache where key=?", key);
			if(StrUtil.isNotBlank(value)) {
				return JSONUtil.toBean(value, resultType);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	/**
	 * 根据key获取缓存列表数据
	 * @param key
	 * @param resultType
	 * @return
	 */
	public <T> List<T> getListByKey(String key, Class<T> resultType) {
		try {
			String value =  Db.use(cds).queryString("select value from cache where key=?", key);
			if(StrUtil.isNotBlank(value)) {
				JSONUtil.toList(JSONUtil.parseArray(value), resultType);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 * 根据key获取缓存数据
	 * @param key
	 * @param resultType
	 * @return
	 */
	public void put(String key, Object value) {
		try {
			if(value != null) {
				Db.use(cds).insertOrUpdate(Entity.create("cache").set("key", key).set("value", JSONUtil.toJsonStr(value)), "key");
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 查询缓存个数
	 * @return
	 */
	public Integer getCount() {
		try {
			return Db.use(cds).queryNumber("select count(*) from cache").intValue();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return 0;
	}
	
	/**
	 * 清除所有缓存
	 */
	public void clear() {
		try {
			Db.use(cds).execute("delete from cache where 1=1");
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 初始化数据库
	 */
	public void init() {
		//初始化缓存数据库
		InputStream stream = getClass().getClassLoader().getResourceAsStream("data/cache_sqlite_init.sql");
		String sql = IoUtil.read(stream, Charset.forName("UTF-8"));
		String[] sqls = sql.split("\n");
		int count = 0;
		try {
			for (String s : sqls) {
				count += Db.use(cds).execute(s);
			}
		} catch (Exception e) {
		}
		logger.info("缓存sqlite初始化成功，影响行数：" + count);
		
	}
	public String getStrByKey(String key) {
		try {
			return  Db.use(cds).queryString("select value from cache where key=?", key);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
}
