package com.onedrivex;

import java.io.File;

import javax.servlet.MultipartConfigElement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.unit.DataSize;

import com.alibaba.druid.pool.DruidDataSource;

import cn.hutool.core.io.FileUtil;
import cn.hutool.db.DbUtil;
import cn.hutool.log.level.Level;

@SpringBootApplication
@EnableCaching
@EnableAsync
@ServletComponentScan
public class App {
	
	@Value("${DATA_TYPE:sqlite}")
	private String dataType;
	@Bean
    public ServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory fa = new TomcatServletWebServerFactory();
        fa.addConnectorCustomizers((TomcatConnectorCustomizer) connector -> connector.setProperty("relaxedQueryChars", "[]{}"));
        return fa;
    }
	
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
	
	@Bean("cache")
	public DruidDataSource getCacheDtaSource() {
		DruidDataSource ds = new DruidDataSource();
		ApplicationHome h = new ApplicationHome(getClass());
        File jarF = h.getSource();
        String path = jarF.getParentFile().toString() + "/data";
        if(!FileUtil.exist(path)) {
        	FileUtil.mkdir(path);
        }
		ds.setDriverClassName("org.sqlite.JDBC");
		ds.setUrl("jdbc:sqlite:"+path+"/cache.db");
		return ds;
	}
	
	@Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        DataSize fileSize = DataSize.ofMegabytes(300);
        DataSize requestSize = DataSize.ofMegabytes(300);
        factory.setMaxFileSize(fileSize); // KB,MB
        factory.setMaxRequestSize(requestSize);
        return factory.createMultipartConfig();
    }
	
	public static void main(String[] args) {
		//DbUtil.setShowSqlGlobal(true, true, true, Level.INFO);
		SpringApplication.run(App.class, args);
	}

}
