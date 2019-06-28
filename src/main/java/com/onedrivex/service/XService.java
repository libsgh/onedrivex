package com.onedrivex.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.alibaba.druid.pool.DruidDataSource;
import com.onedrivex.api.Item;
import com.onedrivex.api.OneDriveApi;
import com.onedrivex.api.TokenInfo;
import com.onedrivex.util.Constants;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.http.HttpUtil;
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
	public String refreshJob(Map<String, String> configMap) {
		String tokenJson = configMap.get(Constants.tokenKey);
		String clientId = configMap.get("clientId");
    	String clientSecret = configMap.get("clientSecret");
    	String redirectUri = configMap.get("redirectUri");
    	if(StrUtil.isNotBlank(tokenJson)) {
    		TokenInfo ti = JSONUtil.toBean(tokenJson, TokenInfo.class);
    		OneDriveApi api = OneDriveApi.getInstance();
    		String newToken = api.refreshToken(ti.getRefresh_token(), clientId, clientSecret, redirectUri);
    		if(logger.isDebugEnabled()) {
    			logger.debug("access_token刷新成功！");
    		}
    		this.updateConfig(Constants.tokenKey, newToken);
    		return newToken;
    	}
    	return "";
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
	
	//@Cacheable(key="#path", value="dir")
	@SuppressWarnings("unchecked")
	public List<Item> getDir(TokenInfo tokenInfo, String path){
		List<Item> list = (List<Item>)Constants.timedCache.get(Constants.dirCachePrefix+path);
		if(list != null) {
			logger.info("从缓存中读取文件夹数据\t{}", Constants.dirCachePrefix+path);
		}else{
			list = api.getDir(tokenInfo, path);
			Constants.timedCache.put(Constants.dirCachePrefix+path, list);
		}
		return list;
	}
	
	public Item getFile(TokenInfo tokenInfo, String path){
		Item item = (Item)Constants.timedCache.get(Constants.fileCachePrefix+path);
		if(item != null) {
			logger.info("从缓存中读取文件数据\t{}", Constants.fileCachePrefix+path);
		}else{
			item = api.getFile(tokenInfo, path);
			Constants.timedCache.put(Constants.fileCachePrefix+path, item);
		}
		return item;
	}
	
	public List<Item> refreshDirCache(TokenInfo ti, String path){
		List<Item> list = api.getDir(ti, path);
		list.parallelStream().forEach(r->{
			Constants.timedCache.put(Constants.fileCachePrefix+r.getPath(), r);
		});
		logger.info("刷新缓存："+ path);
		Constants.timedCache.put(Constants.dirCachePrefix+path, list);
		return list;
	}
	
	@Async
	public void refreshAllCache(String token) {
		if(StrUtil.isNotBlank(token)) {
			TokenInfo ti = JSONUtil.toBean(token, TokenInfo.class);
			this.refreshCache(ti, "/");
		}
	}
	
	private void refreshCache(TokenInfo ti, String path) {
		List<Item> list = this.refreshDirCache(ti, path);
		for (Item item : list) {
			if(item.getFolder()) {
				this.refreshCache(ti, item.getPath());
			}
		}
	}

	public Object getReadme(TokenInfo ti, String path) {
		String c = (String)Constants.timedCache.get(Constants.contentCachePrefix+path);
		if(StrUtil.isNotBlank(c)) {
			return c;
		}else {
			c = HttpUtil.downloadString(this.getFile(ti, path).getDownloadUrl(), "UTF-8");
			Constants.timedCache.put(Constants.contentCachePrefix+path, c);
		}
		return c;
	}
	
}
