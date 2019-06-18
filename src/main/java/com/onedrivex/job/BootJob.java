package com.onedrivex.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import com.onedrivex.api.OneDriveApi;
import com.onedrivex.api.TokenInfo;
import com.onedrivex.util.Constants;

import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;

@Service
public class BootJob  implements  ApplicationListener<ContextRefreshedEvent> {
	
	private final static Logger logger = LoggerFactory.getLogger(BootJob.class);
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent cre) {
		CronUtil.schedule("0 0/15 * * * ?", new Task() {
		    @Override
		    public void execute() {
		    	String tokenJson = Constants.tokenCache.get(Constants.tokenKey, false);
		    	if(StrUtil.isNotBlank(tokenJson)) {
		    		TokenInfo ti = JSONUtil.toBean(tokenJson, TokenInfo.class);
		    		OneDriveApi api = OneDriveApi.getInstance();
		    		String newToken = api.refreshToken(ti.getRefresh_token(), Constants.clientId, Constants.clientSecret, Constants.redirectUri);
		    		if(logger.isDebugEnabled()) {
		    			logger.debug("access_token刷新成功\t{}", newToken);
		    		}
		    		Constants.tokenCache.put(Constants.tokenKey, newToken);
		    	}
		    }
		});
		CronUtil.start();
	}

}
