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

import com.onedrivex.api.OneDriveApi;
import com.onedrivex.api.TokenInfo;
import com.onedrivex.service.DbService;
import com.onedrivex.util.Constants;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;

@Service
public class BootJob  implements  ApplicationListener<ContextRefreshedEvent> {
	
	private final static Logger logger = LoggerFactory.getLogger(BootJob.class);
	
	@Value("${DATA_TYPE:sqlite}")
	private String dataType;
	
	@Autowired
	private DbService servive;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent cre) {
		try {
			//1. 初始化数据库
			InputStream stream = getClass().getClassLoader().getResourceAsStream("data/"+dataType.toLowerCase() + "_init.sql");
			File targetFile = new File(dataType.toLowerCase() + "_init.sql");
			FileUtil.writeFromStream(stream, targetFile);
			List<String> sqls = FileUtil.readLines(targetFile, Charset.forName("UTF-8"));
			logger.info(dataType + "初始化成功，影响行数：" + servive.execBatch(sqls));
			String cron = servive.getConfig("refreshTokenCron");
			CronUtil.schedule(cron, new Task() {
			    @Override
			    public void execute() {
			    	Map<String, String> configMap = servive.getConfigMap();
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
			    		servive.updateConfig(Constants.tokenKey, newToken);
			    	}
			    }
			});
			CronUtil.start();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
