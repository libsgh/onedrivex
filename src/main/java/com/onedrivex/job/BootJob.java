package com.onedrivex.job;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import com.onedrivex.service.DbCacheService;
import com.onedrivex.service.TaskService;
import com.onedrivex.service.XService;
import com.onedrivex.util.Constants;

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
	
	@Autowired
	private DbCacheService cacheServive;
	
	@Autowired
	private TaskService taskService;
	
	public boolean flag = false;
	
	public boolean u_flag = false;
	
	public boolean r_flag = false;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent cre) {
			servive.init();
			taskService.init();
			cacheServive.init();
			Map<String, String> configMap = servive.getConfigMap();
			Constants.globalConfig = configMap;
			String cron = configMap.get("refreshTokenCron");//令牌刷新cron
			String hkac = configMap.get("herokuKeepAliveCron");//heroku防休眠cron
			String hkaa = configMap.get("herokuKeepAliveAddress");//heroku防休眠地址
			String rcc = configMap.get("refreshCacheCron");//刷新缓存cron
			String openCache = configMap.get("openCache");
			servive.refreshJob();
			CronUtil.schedule(cron, new Task() {
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
				Constants.herokuTaskId = CronUtil.schedule(hkac, new Task() {
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
			if(openCache.equals("0")) {
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
			}
			//Constants.timedCache.schedulePrune(Long.parseLong(configMap.get("cacheExpireTime"))*1000);
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
			//每小时清空一次任务列表
			CronUtil.schedule("0 0 0/1 * * ?", new Task() {
				@Override
				public void execute() {
					if(r_flag) {
						return;
					}
					try {
						r_flag = true;
						taskService.clear();
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}finally {
						r_flag = false;
					}
				}
			});
			CronUtil.start();
	}

}
