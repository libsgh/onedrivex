package com.onedrivex.service;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.alibaba.druid.pool.DruidDataSource;
import com.onedrivex.api.Item;
import com.onedrivex.api.OneDriveApi;
import com.onedrivex.api.Task;
import com.onedrivex.api.TokenInfo;
import com.onedrivex.api.UploadInfo;
import com.onedrivex.util.CommonUtil;
import com.onedrivex.util.Constants;
import com.onedrivex.util.SplitFile;

import cn.hutool.core.date.BetweenFormater;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.LineHandler;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

@Service
public class XService {
	
	private final static Logger logger = LoggerFactory.getLogger(XService.class);
	
	private OneDriveApi api = OneDriveApi.getInstance();
	
	@Autowired
	private DruidDataSource ds;
	
	@Resource(name = "task")
	private DruidDataSource tds;
	
	@Autowired
	private DbCacheService cacheService;
	
	@Autowired
	private TaskService taskService;
	
	@Value("${DATA_TYPE:sqlite}")
	private String dataType;
	
	/**
	 * 批量执行sql
	 * @param sqls
	 * @return
	 */
	public void init() {
		InputStream stream = getClass().getClassLoader().getResourceAsStream("data/"+dataType.toLowerCase() + "_init.sql");
		List<String> list = new ArrayList<String>();
		IoUtil.readLines(stream, Charset.forName("UTF-8"), new LineHandler() {
			@Override
			public void handle(String line) {
				list.add(line);
			}
		});
		int count = 0;
		for (String sql : list) {
			try {
				count += Db.use(ds).execute(sql);
			} catch (Exception e) {
			}
		}
		logger.info(dataType + "初始化成功，影响行数：" + count);
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
	public List<Item> getDir(TokenInfo tokenInfo, String path, Boolean isThumbnail){
		List<Item> list = cacheService.getListByKey(Constants.dirCachePrefix+path, Item.class);
		if(list != null) {
			logger.debug("从缓存中读取文件夹数据\t{}", Constants.dirCachePrefix+path);
		}else{
			list = api.getDir(tokenInfo, path);
			cacheService.put(Constants.dirCachePrefix+path, list);
		}
		if(isThumbnail) {
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
		}
		return list;
	}
	
	public Item getFile(TokenInfo tokenInfo, String path){
		Item item = cacheService.getOneByKey(Constants.fileCachePrefix+path, Item.class);
		if(item != null) {
			logger.debug("从缓存中读取文件数据\t{}", Constants.fileCachePrefix+path);
		}else{
			item = api.getFile(tokenInfo, path);
			cacheService.put(Constants.fileCachePrefix+path, item);
		}
		return item;
	}
	
	public List<Item> refreshDirCache(TokenInfo ti, String path){
		List<Item> list = api.getDir(ti, path);
		list.stream().forEach(r -> {
			cacheService.put(Constants.fileCachePrefix+r.getPath(), r);
		});
		//logger.info("刷新缓存："+ path);
		cacheService.put(Constants.dirCachePrefix+path, list);
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
		logger.debug("缓存刷新开始");
		Long start = System.currentTimeMillis();
		if(StrUtil.isNotBlank(token)) {
			TokenInfo ti = JSONUtil.toBean(token, TokenInfo.class);
			this.refreshCache(ti, "/");
		}
		Constants.refreshInfo = "刷新时间："+DateUtil.now()+"，耗时："+ new BetweenFormater((System.currentTimeMillis()-start), BetweenFormater.Level.SECOND, 2).format()+"，缓存"+cacheService.getCount()+"个对象";
		logger.debug(Constants.refreshInfo);
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
		String c = cacheService.getStrByKey(Constants.contentCachePrefix+path);
		if(StrUtil.isNotBlank(c)) {
			return c;
		}else {
			c = HttpUtil.downloadString(this.getFile(ti, path).getDownloadUrl(), "UTF-8");
			cacheService.put(Constants.contentCachePrefix+path, c);
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
		String hkaa = config.get("herokuKeepAliveAddress");//heroku防休眠地址
		String hkac = config.get("herokuKeepAliveCron");//heroku防休眠cron
		if(StrUtil.isNotBlank(hkaa) && StrUtil.isNotBlank(hkac)) {
			CronUtil.remove(Constants.herokuTaskId);
			Constants.herokuTaskId = CronUtil.schedule(hkac, new cn.hutool.cron.task.Task() {
				@Override
				public void execute() {
					try {
						int statusCode = HttpRequest.get(hkaa).execute().getStatus();
						if(logger.isDebugEnabled()) {
							logger.debug("heroku防休眠>>>状态码："+statusCode);
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			});
		}
		if(c > 0) {
			//更新全局变量
			Constants.globalConfig = this.getConfigMap();
		}
		return c;
	}

	public void upload() {
		String localPath = Constants.globalConfig.get("localPath");
		String rootPath = Constants.globalConfig.get("uploadPath");
		upload(localPath, rootPath, true);
	}
	
	@Async
	public void upload(String local, String remote, Boolean isDelete) {
		if(StrUtil.isNotBlank(local) && FileUtil.isNotEmpty(new File(local))) {
			List<File> list = FileUtil.loopFiles(local);
			list.stream().forEach(file->{
				String splitPath = local + File.separator + "split";
				if(!file.getParent().equals(splitPath)) {
					String subPath = FileUtil.subPath(local, file.getPath());
					long length = FileUtil.size(file);
					Task task = new Task();
					task.setRemotePath(remote+"/"+subPath);
					task.setFileSize(length);
					task.setStatus(0);
					taskService.put(remote+"/"+subPath, task);
					//Constants.uploadRecordCache.put(remote+"/"+subPath, task);
					String tokenJson = Constants.globalConfig.get(Constants.tokenKey);
					TokenInfo ti = JSONUtil.toBean(tokenJson, TokenInfo.class);
					SplitFile sc = new SplitFile(file, Constants.splitFileSize);//15.625MB
					sc.init();
					if(FileUtil.exist(splitPath)) {
						FileUtil.mkdir(splitPath);
					}
					List<UploadInfo> uis = sc.spiltfile(splitPath);
					logger.debug("文件名称："+remote+"/"+subPath);
					task.setHumanFileSize(CommonUtil.getFormatSize(file.length()));
					String upLoadUrl = api.createUploadSession(remote+"/"+subPath, ti);
					task.setUploadUrl(upLoadUrl);
					for (UploadInfo uploadInfo : uis) {
						//Task t = ((Task)Constants.uploadRecordCache.get(remote+"/"+subPath));
						Task t = taskService.get(remote+"/"+subPath);
						if(t.getStatus() == 4) {
							continue;
						}
						//分片上传文件
						Long start = System.currentTimeMillis();
						JSONObject jsonObject = api.upload(uploadInfo, upLoadUrl, ti, length);
						String itemId = jsonObject.getStr("id");
						if(itemId != null) {
							task.setItemId(itemId);
							task.setStatus(2);
							task.setUploadSize(length);//下载完成
						}else {
							task.setStatus(1);
							task.setUploadSize(uploadInfo.getEnd());
						}
						Long du = (System.currentTimeMillis() - start)/1000;//秒
						try {
							task.setSpeed(CommonUtil.getFormatSize(NumberUtil.div(length+"", du+"").doubleValue())+"/S");
						} catch (Exception e) {
							task.setSpeed("-");
						}
						taskService.put(remote+"/"+subPath, task);
					}
					//上传成功删除文件
					uis.parallelStream().forEach(f->{
						FileUtil.del(f.getFile());
					});
					if(isDelete) {
						FileUtil.del(file);
					}
				}
			});
		}
		
	}
	
	/**
	 * 删除任务和文件
	 * @param itemId
	 * @param path
	 */
	public void removeTask(String itemId, String path) {
		String tokenJson = Constants.globalConfig.get(Constants.tokenKey);
		TokenInfo ti = JSONUtil.toBean(tokenJson, TokenInfo.class);
		api.delItem(itemId, ti);
		taskService.remove(path);
		//Constants.uploadRecordCache.remove(path);
	}
	
	/**
	 * 取消上传任务
	 * @param path
	 */
	public void cancelTask(String path, String uploadUrl) {
		api.delUploadSession(uploadUrl);
		//Task task = ((Task)Constants.uploadRecordCache.get(path));
		Task task = taskService.get(path);
		task.setStatus(4);
		taskService.put(path, task);
		//Constants.uploadRecordCache.put(path, task);
	}

	public boolean reset() {
		try {
			Db.use(ds).execute("delete from config");
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		this.init();
		cacheService.clear();
		//Constants.uploadRecordCache.clear();
		Constants.tokenCache.clear();
		Constants.globalConfig = this.getConfigMap();
		return true;
	}
	@Async
	public void uploadASync(File file) {
		String local = Constants.globalConfig.get("localPath");
		String remote = Constants.globalConfig.get("uploadPath");
		String splitPath = local + File.separator + "split";
		if(!file.getParent().equals(splitPath)) {
			String subPath = FileUtil.subPath(local, file.getPath());
			long length = FileUtil.size(file);
			String tokenJson = Constants.globalConfig.get(Constants.tokenKey);
			TokenInfo ti = JSONUtil.toBean(tokenJson, TokenInfo.class);
			SplitFile sc = new SplitFile(file, Constants.splitFileSize);//15.625MB
			sc.init();
			if(FileUtil.exist(splitPath)) {
				FileUtil.mkdir(splitPath);
			}
			List<UploadInfo> uis = sc.spiltfile(splitPath);
			logger.debug("文件名称："+remote+"/"+subPath);
			String upLoadUrl = api.createUploadSession(remote+"/"+subPath, ti);
			for (UploadInfo uploadInfo : uis) {
				try {
					api.upload(uploadInfo, upLoadUrl, ti, length);
				} catch (Exception e) {
					api.upload(uploadInfo, upLoadUrl, ti, length);
				}
			}
			//上传成功删除文件
			uis.parallelStream().forEach(f->{
				FileUtil.del(f.getFile());
			});
			FileUtil.del(file);
		}
	}

}
