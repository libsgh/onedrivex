package com.onedrivex.service;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.alibaba.druid.pool.DruidDataSource;
import com.onedrivex.api.Item;
import com.onedrivex.api.OneDriveApi;
import com.onedrivex.api.TokenInfo;
import com.onedrivex.api.UploadInfo;
import com.onedrivex.util.Constants;
import com.onedrivex.util.SplitFile;

import cn.hutool.core.io.FileUtil;
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
			logger.debug("从缓存中读取文件夹数据\t{}", Constants.dirCachePrefix+path);
		}else{
			list = api.getDir(tokenInfo, path);
			Constants.timedCache.put(Constants.dirCachePrefix+path, list);
		}
		list = list.parallelStream().map(r->{
			String t = null;
			if(!r.getFolder() && r.getFileType().equals("audio")) {
				t = StrUtil.subBefore(api.thumbnail(tokenInfo, r.getPath(), "large"), "&width=", true);
			}else if(!r.getFolder() && !r.getFileType().equals("audio")){
				t = api.thumbnail(tokenInfo, r.getPath(), "large");
			}
			r.setThumb(t);
			return r;
		}).collect(Collectors.toList());
		return list;
	}
	
	public Item getFile(TokenInfo tokenInfo, String path){
		Item item = (Item)Constants.timedCache.get(Constants.fileCachePrefix+path);
		if(item != null) {
			logger.debug("从缓存中读取文件数据\t{}", Constants.fileCachePrefix+path);
		}else{
			item = api.getFile(tokenInfo, path);
			Constants.timedCache.put(Constants.fileCachePrefix+path, item);
		}
		return item;
	}
	
	public List<Item> refreshDirCache(TokenInfo ti, String path){
		List<Item> list = api.getDir(ti, path);
		list.stream().parallel().forEach(r->{
			Constants.timedCache.put(Constants.fileCachePrefix+r.getPath(), r);
		});
		//logger.info("刷新缓存："+ path);
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
	public void refreshCacheJob(String token) {
		logger.info("缓存刷新开始");
		Long start = System.currentTimeMillis();
		if(StrUtil.isNotBlank(token)) {
			TokenInfo ti = JSONUtil.toBean(token, TokenInfo.class);
			this.refreshCache(ti, "/");
		}
		logger.info("缓存刷新完成，耗时："+(System.currentTimeMillis()-start)+"毫秒，缓存"+Constants.timedCache.size()+"个对象");
	}
	
	private void refreshCache(TokenInfo ti, String path) {
		List<Item> list = this.refreshDirCache(ti, path);
		for (Item item : list) {
			if(item.getFolder() && !Constants.globalConfig.get("onedriveHide").contains(item.getPath())) {
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

	public String updatePass(String old_pass, String password, String password2) {
		Map<String, String> config = new HashMap<String, String>();
		if(StrUtil.isBlank(old_pass)) {
			return "请输入原始密码";
		}
		if(StrUtil.isBlank(password)) {
			return "请输入修改密码";
		}
		if(StrUtil.isBlank(password2)) {
			return "请输入确认密码";
		}
		if(!password2.equals(password)) {
			return "两次输入的密码不一致";
		}
		if(!old_pass.equals(getConfig("password"))) {
			return "请输入正确的原始密码";
		}
		config.put("password", password);
		int c = updateConfig(config);
		if(c > 0) {
			return "密码修改成功";
		}
		return "密码修改失败";
	}
	
	public int updateConfig(Map<String, String> config) {
		int c = config.keySet().parallelStream().map(key->{
			String value = config.get(key);
			try {
				if(StrUtil.isNotBlank(key)) {
					return Db.use(ds).execute("update config set value =? where key = ?", value, key);
				}
			} catch (SQLException e) {
			}
			return 0;
		}).collect(Collectors.summingInt(r->r));
		if(c > 0) {
			//更新全局变量
			Constants.globalConfig = this.getConfigMap();
		}
		return c;
	}

	public void upload() {
		String rootPath = Constants.globalConfig.get("uploadPath");
		if(StrUtil.isNotBlank(rootPath) && FileUtil.isNotEmpty(new File(rootPath))) {
			List<File> list = FileUtil.loopFiles(rootPath);
			list.stream().forEach(file->{
				String tokenJson = Constants.globalConfig.get(Constants.tokenKey);
				TokenInfo ti = JSONUtil.toBean(tokenJson, TokenInfo.class);
				SplitFile sc = new SplitFile(file, Constants.splitFileSize);//15.625MB
			    sc.init();
			    String splitPath = rootPath + File.separator + "split";
			    if(FileUtil.exist(splitPath)) {
			    	FileUtil.mkdir(splitPath);
			    }
			    List<UploadInfo> uis = sc.spiltfile(splitPath);
			    System.out.println(file.getParent()+File.separator+file.getName());
			    String upLoadUrl = api.createUploadSession(file.getParent()+File.separator+file.getName(), ti);
			    long length = FileUtil.size(file);
			    for (UploadInfo uploadInfo : uis) {
			    	//分片上传文件
			    	api.upload(uploadInfo, upLoadUrl, ti, length);
				}
			    //上传成功删除文件
			    uis.parallelStream().forEach(f->{
			    	FileUtil.del(f.getFile());
			    });
			    FileUtil.del(file);
			});
		}
	}
	
}
