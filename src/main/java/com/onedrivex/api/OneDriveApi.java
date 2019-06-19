package com.onedrivex.api;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import com.onedrivex.common.CommonUtil;
import com.onedrivex.util.Constants;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;

public class OneDriveApi {
	
	public  static volatile OneDriveApi api;
		
	public static OneDriveApi getInstance() {
        if (api == null) {
            synchronized (OneDriveApi.class) {
                if (api == null) {
                	api = new OneDriveApi();
                }
            }
        }
        return api;
	}
	
	public static void main(String[] args) {
		OneDriveApi oneDrive = new OneDriveApi();
		TokenInfo tokenInfo = new TokenInfo();
		tokenInfo.setToken_type("Bearer");
		tokenInfo.setAccess_token("eyJ0eXAiOiJKV1QiLCJub25jZSI6IkFRQUJBQUFBQUFEQ29NcGpKWHJ4VHE5Vkc5dGUtN0ZYNXBnX2VNd0YwVldrZzl4aFNFcmQ5X1p0dVJ4b2VlWWVVbDR2ZDlLa1M2UWswSm5BYzVDa1ZJN2NCem5qMUNXWHBNSjU3akl4RnZNaVRwNndtU3Y4UFNBQSIsImFsZyI6IlJTMjU2IiwieDV0IjoiQ3RmUUM4TGUtOE5zQzdvQzJ6UWtacGNyZk9jIiwia2lkIjoiQ3RmUUM4TGUtOE5zQzdvQzJ6UWtacGNyZk9jIn0.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC80NGQ4NzlkYS1mM2VmLTQwMjQtODZlMy1hYmMyZTA3NmU2ODEvIiwiaWF0IjoxNTYwOTMzNjAwLCJuYmYiOjE1NjA5MzM2MDAsImV4cCI6MTU2MDkzNzUwMCwiYWNjdCI6MCwiYWNyIjoiMSIsImFpbyI6IjQyWmdZTGdocWwwcTNyWGVjYjJYckhUdXFiejE1Y0Z5ZnE5dHpLT1Z0TTlVSyt1N1N3TUEiLCJhbXIiOlsicHdkIl0sImFwcF9kaXNwbGF5bmFtZSI6Im9uZWRyaXZleCIsImFwcGlkIjoiNDBhZTY5ZTctYjdkOC00ZjJmLThkNjAtMDgzMWM5YmY5ZDA0IiwiYXBwaWRhY3IiOiIxIiwiZmFtaWx5X25hbWUiOiJpaSIsImdpdmVuX25hbWUiOiJjbSIsImlwYWRkciI6IjExNC4yNDQuMzYuMTMwIiwibmFtZSI6ImlpY20iLCJvaWQiOiJkNDE0NmRlNy1jNzI5LTQ5Y2YtYjI2Yy0yYTRiYzQ1N2U4ZTEiLCJwbGF0ZiI6IjMiLCJwdWlkIjoiMTAwMzNGRkZBRjUxNTg2MCIsInNjcCI6IkZpbGVzLlJlYWRXcml0ZS5BbGwgcHJvZmlsZSBvcGVuaWQgZW1haWwiLCJzaWduaW5fc3RhdGUiOlsia21zaSJdLCJzdWIiOiI0MG56cS1iRGJIZ0hIb2tmQlhRbHFkeUxCWW4wdklSc3pySkE2SUd4a3dVIiwidGlkIjoiNDRkODc5ZGEtZjNlZi00MDI0LTg2ZTMtYWJjMmUwNzZlNjgxIiwidW5pcXVlX25hbWUiOiJpaWNtQG1haWwuaHJrYS5uZXQiLCJ1cG4iOiJpaWNtQG1haWwuaHJrYS5uZXQiLCJ1dGkiOiJiMlQ5RjBnQ0EwQ2JWN05UNDNkU0FBIiwidmVyIjoiMS4wIiwieG1zX3N0Ijp7InN1YiI6Ikl3VmdnWTNILThOMGw1TUFHc3I1SmtFUmFqQzUzazRLT1cxSk12Z2tYM0UifSwieG1zX3RjZHQiOjE1MjYxMjcyOTl9.TXXa1dnu7yAdhXJKYg-tCcEerFoCKE3C2Cn0KqoxVCwVfyM-b0k_cGpjaTOrwIyuS7QU6_JoLIc8WPBY-IaT1E6q6pqqBnWXh743EZI7QqKsDKyHk-ev-U7yCKkEAzaAAWB0qYF6mIJEsGuUlD7gqD2u1p95cPKlZPA_4G5iXDCPhB8Kz8TGzqKypjAL7iLxRTnuCqKPxoAFWA7MEDFr1wiW_JPA4o8cZZHHeh6HDopjVMli35KUbPpZG11quzrA8HzsLiP6xUm9K5b-m0nFs2OR9N1VvVR1k9MFxA8QCVZobdHJE8O7SZ_rRdog7h1PVZ2rwGYeozacnZoypulZQQ");
		oneDrive.getRootDir(tokenInfo);
	}
	
	public String oauth2(String cliendId, String redirectUri) {
		return String.format("https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id=%s&scope=%s&response_type=code&redirect_uri=%s", cliendId, Constants.scope, URLUtil.encode(redirectUri));
	}
	public String quickStartRegUrl(String redirectUri) {
		String ru = "https://developer.microsoft.com/en-us/graph/quick-start?appID=_appId_&appName=_appName_&redirectUrl="+redirectUri+"&platform=option-php";
		String deepLink = "/quickstart/graphIO?publicClientSupport=false&appName=onedrivex&redirectUrl="+redirectUri+"&allowImplicitFlow=false&ru="+URLUtil.encodeAll(ru);
		String appUrl = "https://apps.dev.microsoft.com/?deepLink="+URLUtil.encodeAll(deepLink);
		return appUrl;
	}
	
	/**
	 * 获取令牌
	 * @param code 授权第一步获取的code
	 * @param clientId 应用的id
	 * @param clientSecret 应用的密钥
	 * @param redirectUri 重定向url，和第一步的url一致
	 * @return
	 */
	public String getToken(String code, String clientId, String clientSecret, String redirectUri) {
		String result = HttpRequest
							.post("https://login.microsoftonline.com/common/oauth2/v2.0/token")
							.header("Content-Type","application/x-www-form-urlencoded")
							.form("client_id", clientId)
							.form("redirect_uri", redirectUri)
							.form("client_secret", clientSecret)
							.form("code", code)
							.form("grant_type", "authorization_code")
							.execute().body();
		return result;
	}
	
	/**
	 * 获取新的访问令牌或刷新令牌
	 * @param code 授权第一步获取的code
	 * @param clientId 应用的id
	 * @param clientSecret 应用的密钥
	 * @param redirectUri 重定向url，和第一步的url一致
	 * @return
	 */
	public String refreshToken(String refreshToken, String clientId, String clientSecret, String redirectUri) {
		String result = HttpRequest
				.post("https://login.microsoftonline.com/common/oauth2/v2.0/token")
				.header("Content-Type","application/x-www-form-urlencoded")
				.form("client_id", clientId)
				.form("redirect_uri", redirectUri)
				.form("client_secret", clientSecret)
				.form("refresh_token", refreshToken)
				.form("grant_type", "refresh_token")
				.timeout(Constants.timeout)
				.execute().body();
		return result;
	}
	
	/**
	 * 获取目录信息
	 * @param tokenInfo
	 * @return
	 */
	public List<Item> getRootDir(TokenInfo tokenInfo) {
		HttpRequest request = request("/", "children?select=name,size,folder,@microsoft.graph.downloadUrl,lastModifiedDateTime", tokenInfo);
		List<Item> items = new ArrayList<Item>();
		this.dirNextPage(items, request, 0);
		return items;
	}
	
	@SuppressWarnings("unchecked")
	public void dirNextPage(List<Item> items, HttpRequest request, int retry) {
		String body = request.execute().body();
		if(StrUtil.isBlank(body) && retry < 3) {
			retry++;
			this.dirNextPage(items, request, retry);
		}
		List<Map<String, Object>> list = (List<Map<String, Object>>) JSONUtil.parse(body).getByPath("$.value");
		for (Map<String, Object> map : list) {
			String time = DateUtil.formatDateTime(DateUtil.parse(map.get("lastModifiedDateTime").toString()));
			String name = map.get("name").toString();
			String downloadUrl = map.get("@microsoft.graph.downloadUrl")!=null?map.get("@microsoft.graph.downloadUrl").toString():null;
			String size = CommonUtil.getFormatSize(Double.parseDouble(map.get("size").toString()));
			Object folder = map.get("folder");
			Integer childCount =0;
			String ext = null;
			if(folder != null) {
				Map<String , Integer> folderMap = (Map<String , Integer>)folder;
				childCount = (Integer)folderMap.get("childCount");
			}else {
				ext = CommonUtil.fileIco(name);
			}
			items.add(new Item(name, size, time, (folder==null?false:true), childCount ,downloadUrl, ext));
		}
	}
	
	/**
	 * 生成一个带令牌的request
	 * @param path 路径
	 * @param query 查询内容
	 * @param tokenInfo 令牌信息 
	 * @return
	 */
	public static HttpRequest request(String path, String query, TokenInfo tokenInfo) {
		return HttpRequest.get(Constants.apiUrl + "/me/drive/root" + path + query)
							.header("Authorization", tokenInfo.getAuth())
							.header("Host", "graph.microsoft.com")
							.timeout(Constants.timeout);
	}
	
}
