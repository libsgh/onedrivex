package com.onedrivex.util;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.date.DateUnit;

public class Constants {
	
	public static String scope = "offline_access files.readwrite.all";
	
	public static String tokenKey = "onedrive_token";//redis key
	
	public static String redirectUri = "http://localhost:8080/authRedirect";//重定向url
	
	public static String clientId = "0830c3fd-65ab-4247-9145-8ca8e32eea44";//客户端id
	
	public static String clientSecret = "mdeeMXTEIQT0981=(!gme0{";//客户端密钥
	
	public static String oauth2 = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id=%s&scope=%s&response_type=code&redirect_uri=%s";//oauth2
	
	public static TimedCache<String, String> tokenCache = CacheUtil.newTimedCache(DateUnit.SECOND.getMillis() * 3000);
	
	public static String apiUrl = "https://graph.microsoft.com/v1.0/me";//客户端id

	public static int timeout = 5000;//http连接请求超时时间
	
}
