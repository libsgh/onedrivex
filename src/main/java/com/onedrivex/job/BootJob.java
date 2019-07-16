package com.onedrivex.job;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import com.onedrivex.service.XService;
import com.onedrivex.util.Constants;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.http.HttpRequest;

@Service
public class BootJob  implements  ApplicationListener<ContextRefreshedEvent> {
	
	private final static Logger logger = LoggerFactory.getLogger(BootJob.class);
	
	@Value("${DATA_TYPE:sqlite}")
	private String dataType;
	
	@Autowired
	private XService servive;
	
	public boolean flag = false;
	
	public boolean u_flag = false;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent cre) {
			//1. 初始化数据库
			InputStream stream = getClass().getClassLoader().getResourceAsStream("data/"+dataType.toLowerCase() + "_init.sql");
			File targetFile = new File(dataType.toLowerCase() + "_init.sql");
			FileUtil.writeFromStream(stream, targetFile);
			List<String> sqls = FileUtil.readLines(targetFile, Charset.forName("UTF-8"));
			logger.info(dataType + "初始化成功，影响行数：" + servive.execBatch(sqls));
			Map<String, String> configMap = servive.getConfigMap();
			Constants.globalConfig = configMap;
			String cron = configMap.get("refreshTokenCron");//令牌刷新cron
			String hkac = configMap.get("herokuKeepAliveCron");//heroku防休眠cron
			String hkaa = configMap.get("herokuKeepAliveAddress");//heroku防休眠地址
			String rcc = configMap.get("refreshCacheCron");//刷新缓存cron
			Constants.refreshCacheTaskId = CronUtil.schedule(cron, new Task() {
			    @Override
			    public void execute() {
			    	try {
			    		servive.refreshJob();
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
			    }
			});
			if(StrUtil.isNotBlank(hkaa)) {
				CronUtil.schedule(hkac, new Task() {
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
			//刷新所有缓存
			//servive.refreshAllCache(token);
			CronUtil.schedule(rcc, new Task() {
				@Override
				public void execute() {
					if(flag) {
						return;
					}
					try {
						flag = true;
						String tokenJson = servive.getConfig(Constants.tokenKey);
						servive.refreshCacheJob(tokenJson);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}finally {
						flag = false;
					}
				}
			});
			Constants.timedCache.schedulePrune(Long.parseLong(configMap.get("cacheExpireTime"))*1000);
			CronUtil.schedule("0 0/1 * * * ?", new Task() {
				@Override
				public void execute() {
					if(u_flag) {
						return;
					}
					try {
						u_flag = true;
						servive.upload();
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}finally {
						u_flag = false;
					}
				}
			});
			CronUtil.start();
	}

}
