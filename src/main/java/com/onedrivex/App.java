package com.onedrivex;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;

import com.alibaba.druid.pool.DruidDataSource;

import cn.hutool.core.io.FileUtil;

@SpringBootApplication
@EnableCaching
@EnableAsync
@ServletComponentScan
public class App {
	
	@Value("${DATA_TYPE:sqlite}")
	private String dataType;
	
	@Bean
	@Primary
	public DruidDataSource getDataSource() {
		DruidDataSource ds = new DruidDataSource();
		if(dataType.equalsIgnoreCase("postgresql")) {
			ds.setDriverClassName("org.postgresql.Driver");
			ds.setUrl(System.getenv("JDBC_DATABASE_URL"));
			ds.setUsername(System.getenv("JDBC_DATABASE_USERNAME"));
			ds.setPassword(System.getenv("JDBC_DATABASE_PASSWORD"));
		}else{
			ApplicationHome h = new ApplicationHome(getClass());
	        File jarF = h.getSource();
	        String path = jarF.getParentFile().toString() + "/data";
	        if(!FileUtil.exist(path)) {
	        	FileUtil.mkdir(path);
	        }
			ds.setDriverClassName("org.sqlite.JDBC");
			//ds.setUrl("jdbc:sqlite::resource:data/onedrivex.db");
			ds.setUrl("jdbc:sqlite:"+path+"/onedrivex.db");
		}
		return ds;
	}
	
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

}
