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
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent cre) {
		try {
			//1. 初始化数据库
			InputStream stream = getClass().getClassLoader().getResourceAsStream("data/"+dataType.toLowerCase() + "_init.sql");
			File targetFile = new File(dataType.toLowerCase() + "_init.sql");
			FileUtil.writeFromStream(stream, targetFile);
			List<String> sqls = FileUtil.readLines(targetFile, Charset.forName("UTF-8"));
			logger.info(dataType + "初始化成功，影响行数：" + servive.execBatch(sqls));
			Map<String, String> configMap = servive.getConfigMap();
			String cron = configMap.get("refreshTokenCron");
			String hkac = configMap.get("herokuKeepAliveCron");
			String hkaa = configMap.get("herokuKeepAliveAddress");
			servive.refreshJob(configMap);
			CronUtil.schedule(cron, new Task() {
			    @Override
			    public void execute() {
			    	servive.refreshJob();
			    }
			});
			if(StrUtil.isNotBlank(hkaa)) {
				CronUtil.schedule(hkac, new Task() {
					@Override
					public void execute() {
						int statusCode = HttpRequest.get(hkaa).execute().getStatus();
						if(logger.isDebugEnabled()) {
							logger.debug("heroku防休眠>>>状态码："+statusCode);
						}
					}
				});
			}
			CronUtil.start();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
