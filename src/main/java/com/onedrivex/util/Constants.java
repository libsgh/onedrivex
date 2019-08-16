package com.onedrivex.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.date.DateUnit;

public class Constants {
	
	public static final String tokenKey = "tokenInfo";

	public static String scope = "offline_access files.readwrite.all";
	
	public static String oauth2 = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id=%s&scope=%s&response_type=code&redirect_uri=%s";//oauth2
	
	public static TimedCache<String, String> tokenCache = CacheUtil.newTimedCache(DateUnit.SECOND.getMillis() * 3000);
	
	public static String apiUrl = "https://graph.microsoft.com/v1.0";//客户端id

	public static int timeout = 50000;//http连接请求超时时间
	
	public static Map<String, String> globalConfig = new ConcurrentHashMap<String, String>();
	
	//public static TimedCache<String, Object> timedCache = CacheUtil.newTimedCache(-1);
	
	public static TimedCache<String, Object> uploadRecordCache = CacheUtil.newTimedCache(3600000);
	
	public static String dirCachePrefix = "dir_";
	
	public static String contentCachePrefix = "c_";
	
	public static String fileCachePrefix = "file_";
	
	public static String refreshCacheTaskId = "";
	
	public static long splitFileSize = 16384000;

	public static String herokuTaskId = "";

	public static String refreshInfo = "-";
	
}
