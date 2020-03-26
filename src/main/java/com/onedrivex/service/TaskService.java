package com.onedrivex.service;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.druid.pool.DruidDataSource;
import com.onedrivex.api.Task;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.LineHandler;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.json.JSONUtil;

@Service
public class TaskService {
	
	private final static Logger logger = LoggerFactory.getLogger(TaskService.class);
	
	@Resource(name = "task")
	private DruidDataSource tds;
	
	/**
	 * 根据key更新或新增任务
	 * @param key
	 * @param resultType
	 * @return
	 */
	public void put(String key, Task task) {
		try {
			if(task != null) {
				Db.use(tds).insertOrUpdate(Entity.create("task").set("key", key).set("value", JSONUtil.toJsonStr(task)), "key");
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 任务个数
	 * @return
	 */
	public Integer getCount() {
		try {
			return Db.use(tds).queryNumber("select count(*) from task").intValue();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return 0;
	}
	
	/**
	 * 清除所有任务
	 */
	public void clear() {
		try {
			Db.use(tds).execute("delete from task where 1=1");
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 初始化数据库
	 */
	public void init() {
		//初始化缓存数据库
		InputStream stream = getClass().getClassLoader().getResourceAsStream("data/task_sqlite_init.sql");
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
				count += Db.use(tds).execute(sql);
			} catch (Exception e) {
			}
		}
		logger.info("上传任务sqlite初始化成功，影响行数：" + count);
		
	}
	
	/**
	 * 根据key获取一个任务
	 * @param key
	 * @return
	 */
	public Task get(String key) {
		try {
			String taskStr = Db.use(tds).queryString("select value from task where key=?", key);
			return JSONUtil.toBean(taskStr, Task.class);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return new Task();
	}
	
	/**
	 * 根据key删除一个任务
	 * @param path
	 */
	public void remove(String key) {
		try {
			Db.use(tds).execute("delete from task where key=?", key);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 任务列表
	 * @return
	 */
	public List<Task> taskList() {
		try {
			List<Entity> list = Db.use(tds).query("select * from task where 1=1");
			return list.parallelStream().map(r->{
				return JSONUtil.toBean(r.getStr("value"), Task.class);
			}).collect(Collectors.toList());
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		return new ArrayList<Task>();
	}
	
}
