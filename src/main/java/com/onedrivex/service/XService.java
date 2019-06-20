package com.onedrivex.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.alibaba.druid.pool.DruidDataSource;
import com.onedrivex.api.Item;
import com.onedrivex.api.OneDriveApi;
import com.onedrivex.api.TokenInfo;
import com.onedrivex.util.Constants;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.json.JSONUtil;

@Service
public class XService {
	
	private final static Logger logger = LoggerFactory.getLogger(XService.class);
	
	private OneDriveApi api = OneDriveApi.getInstance();
	
	@Autowired
	private DruidDataSource ds;
	
	/**
	 * 批量执行sql
	 * @param sqls
	 * @return
	 */
	public int execBatch(List<String> sqls) {
		int count = 0;
		for (String sql : sqls) {
			try {
				count += Db.use(ds).execute(sql);
			} catch (Exception e) {
			}
		}
		return count;
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
	
	/*
	 * 获取所有配置信息
	 */
	public Map<String, String> getConfigMap() {
		Map<String, String> map = new HashMap<String,String>();
		try {
			List<Entity> list = Db.use(ds).query("select * from config where 1=1");
			for (Entity entity : list) {
				map.put(entity.getStr("key"), entity.getStr("value"));
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return map;
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
	
	/**
	 * 令牌刷新任务
	 */
	public void refreshJob(Map<String, String> configMap) {
		String tokenJson = configMap.get(Constants.tokenKey);
		String clientId = configMap.get("clientId");
    	String clientSecret = configMap.get("clientSecret");
    	String redirectUri = configMap.get("redirectUri");
    	if(StrUtil.isNotBlank(tokenJson)) {
    		TokenInfo ti = JSONUtil.toBean(tokenJson, TokenInfo.class);
    		OneDriveApi api = OneDriveApi.getInstance();
    		String newToken = api.refreshToken(ti.getRefresh_token(), clientId, clientSecret, redirectUri);
    		if(logger.isDebugEnabled()) {
    			logger.debug("access_token刷新成功\t{}", newToken);
    		}
    		this.updateConfig(Constants.tokenKey, newToken);
    	}
	}
	
	/**
	 * 令牌刷新任务
	 */
	public void refreshJob() {
		Map<String, String> configMap = this.getConfigMap();
		String tokenJson = configMap.get(Constants.tokenKey);
		String clientId = configMap.get("clientId");
		String clientSecret = configMap.get("clientSecret");
		String redirectUri = configMap.get("redirectUri");
		if(StrUtil.isNotBlank(tokenJson)) {
			TokenInfo ti = JSONUtil.toBean(tokenJson, TokenInfo.class);
			OneDriveApi api = OneDriveApi.getInstance();
			String newToken = api.refreshToken(ti.getRefresh_token(), clientId, clientSecret, redirectUri);
			if(logger.isDebugEnabled()) {
				logger.debug("access_token刷新成功\t{}", newToken);
			}
			this.updateConfig(Constants.tokenKey, newToken);
		}
	}
	
	@Cacheable(key="#path", value="dir")
	public List<Item> getDir(TokenInfo tokenInfo, String path){
		return api.getDir(tokenInfo, path);
	}
	
	@CachePut(key="#path", value="dir")
	public List<Item> refreshDirCache(TokenInfo tokenInfo, String path){
		return api.getDir(tokenInfo, path);
	}
	
}
