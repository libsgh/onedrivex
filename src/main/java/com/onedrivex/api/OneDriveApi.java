package com.onedrivex.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.onedrivex.common.CommonUtil;
import com.onedrivex.util.Constants;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
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
		tokenInfo.setAccess_token("eyJ0eXAiOiJKV1QiLCJub25jZSI6IkFRQUJBQUFBQUFEQ29NcGpKWHJ4VHE5Vkc5dGUtN0ZYdHYwalZCT1B0YndwQThWOXlkWl9YZEt4SDNOdDhlTi1Uc1VqNFZxZ0Q4QUZhUTZDcWlBRm5jVW5kSHhvN2dSdHM3c0VfOWZXelc3ZVB0alo4dEw3TGlBQSIsImFsZyI6IlJTMjU2IiwieDV0IjoiQ3RmUUM4TGUtOE5zQzdvQzJ6UWtacGNyZk9jIiwia2lkIjoiQ3RmUUM4TGUtOE5zQzdvQzJ6UWtacGNyZk9jIn0.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC80NGQ4NzlkYS1mM2VmLTQwMjQtODZlMy1hYmMyZTA3NmU2ODEvIiwiaWF0IjoxNTYxNjk1NDg3LCJuYmYiOjE1NjE2OTU0ODcsImV4cCI6MTU2MTY5OTM4NywiYWNjdCI6MCwiYWNyIjoiMSIsImFpbyI6IjQyWmdZUEEwKzMrZloyNk9TdVNCdllxSjdpZlZoTXgvVjk5N1dCTXVkK0h6YTJHbTIxWUEiLCJhbXIiOlsicHdkIl0sImFwcF9kaXNwbGF5bmFtZSI6Im9uZWRyaXZleCIsImFwcGlkIjoiMjM1YzJkMjMtMzBiZC00OWVlLWFlZmMtZGFjNWMyMTcxMWE0IiwiYXBwaWRhY3IiOiIxIiwiZmFtaWx5X25hbWUiOiJpaSIsImdpdmVuX25hbWUiOiJjbSIsImlwYWRkciI6IjExNC4yNDQuMzYuMTMwIiwibmFtZSI6ImlpY20iLCJvaWQiOiJkNDE0NmRlNy1jNzI5LTQ5Y2YtYjI2Yy0yYTRiYzQ1N2U4ZTEiLCJwbGF0ZiI6IjMiLCJwdWlkIjoiMTAwMzNGRkZBRjUxNTg2MCIsInNjcCI6IkZpbGVzLlJlYWRXcml0ZS5BbGwgcHJvZmlsZSBvcGVuaWQgZW1haWwiLCJzaWduaW5fc3RhdGUiOlsia21zaSJdLCJzdWIiOiI0MG56cS1iRGJIZ0hIb2tmQlhRbHFkeUxCWW4wdklSc3pySkE2SUd4a3dVIiwidGlkIjoiNDRkODc5ZGEtZjNlZi00MDI0LTg2ZTMtYWJjMmUwNzZlNjgxIiwidW5pcXVlX25hbWUiOiJpaWNtQG1haWwuaHJrYS5uZXQiLCJ1cG4iOiJpaWNtQG1haWwuaHJrYS5uZXQiLCJ1dGkiOiJJSGJCbm9UZGJFdVl6b3pvYlZoNUFBIiwidmVyIjoiMS4wIiwieG1zX3N0Ijp7InN1YiI6ImduakZzLXQ2bkJJeGI3R0ZGNEdlRjFtNnUwNnpZUzdiSDBIVlZVNjBnTlUifSwieG1zX3RjZHQiOjE1MjYxMjcyOTl9.XPDMyQD62elZsTZTq2WZtyqO6YvYzuTqluJCPJoDDmMb92n6E1ILxDx2Y8iCYQCdP8oa69hruVJG-4sD-e3kuUePrcKhBCNqgaCNpeXvfUV3hTkFYEJV5VqVcd_3ObIRaAMR_TaPTEeoOlHYJlo4BBDJY9QFbMf0lgIVzRmY4CSzThveyPjLTEAgW2ae9-KXYeGWqU7ycGUntTp_ZG6Vsm_vy_u_F8cM2HJL_8COMDZQM6jbLdadBi8jwO_D97BRAmZHkYz97WX0rv29mE0hsLBOEwT9ymmhiQSRFbyCH5ZOzqMeF3gDDMr_I15EUKqd5TTB98XXJ-NFZ4c-18Zrew");
		//System.out.println(oneDrive.getFile(tokenInfo, "/image/1989362.jpg").toString());
		List<Item> items = oneDrive.getDir(tokenInfo, "/private");
		System.out.println(JSONUtil.toJsonPrettyStr(items));
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
	public List<Item> getDir(TokenInfo tokenInfo, String path) {
		HttpRequest request = request(path, "children?select=id,name,size,folder,@microsoft.graph.downloadUrl,lastModifiedDateTime,file", tokenInfo);
		List<Item> items = new ArrayList<Item>();
		this.dirNextPage(items, request, 0, path, tokenInfo);
		return items;
	}
	
	/**
	 * 获取文件信息
	 * @param tokenInfo
	 * @param path
	 * @return
	 */
	public Item getFile(TokenInfo tokenInfo, String path) {
		String body = HttpRequest.get(Constants.apiUrl + "/me/drive/root:" + path)
					.header("Authorization", tokenInfo.getAuth())
					.header("Host", "graph.microsoft.com")
					.timeout(Constants.timeout).execute().body();
		JSONObject json = JSONUtil.parseObj(body);
		String name = json.getStr("name");
		if(name == null) {
			return null;
		}
		String time = DateUtil.formatDateTime(DateUtil.parse(json.getStr("lastModifiedDateTime").toString()));
		String downloadUrl = json.getStr("@microsoft.graph.downloadUrl")!=null?json.getStr("@microsoft.graph.downloadUrl").toString():null;
		String size = CommonUtil.getFormatSize(Double.parseDouble(json.getStr("size").toString()));
		String icon = null;
		String ext = null;
		Boolean folder = json.get("folder")==null?false:true;
		Integer childCount = 0;
		String t = null;
		String fileType = "folder";
		if(folder) {
			Map<String , Integer> folderMap = (Map<String , Integer>)json.get("folder");
			childCount = (Integer)folderMap.get("childCount");
		}else {
			fileType = ((String)json.getByPath("$.file.mimeType")).split("/")[0];
			ext = CommonUtil.fileType(name);
			icon = CommonUtil.fileIco(name);
			if(fileType.equals("audio")) {
				t = StrUtil.subBefore(thumbnail(tokenInfo, path, "large"), "&width=", true);
			}else{
				t = thumbnail(tokenInfo, path, "large");
			}
		}
		//String t = thumbnail(tokenInfo, path, "large");
		return new Item(name, size, time, folder, childCount ,downloadUrl, ext, icon, path, t, fileType);
	}
	
	/**
	 * 获取文件略缩图
	 * @param tokenInfo
	 * @param path 文件路径
	 * @param size small、medium、large
	 * @return
	 */
	public String thumbnail(TokenInfo tokenInfo, String path, String size) {
		String body = request(path, "thumbnails/0?select="+size,tokenInfo).execute().body();
		JSONObject json = JSONUtil.parseObj(body);
		return (String)json.getByPath("$."+size+".url");
	}
	
	@SuppressWarnings("unchecked")
	public void dirNextPage(List<Item> items, HttpRequest request, int retry, String path, TokenInfo tokenInfo) {
		String body = request.execute().body();
		if(StrUtil.isBlank(body) && retry < 3) {
			retry++;
			this.dirNextPage(items, request, retry, path, tokenInfo);
		}
		List<Map<String, Object>> list = (List<Map<String, Object>>) JSONUtil.parse(body).getByPath("$.value");
		if(list != null) {
			for (Map<String, Object> map : list) {
				String time = DateUtil.formatDateTime(DateUtil.parse(map.get("lastModifiedDateTime").toString()));
				String name = map.get("name").toString();
				String downloadUrl = map.get("@microsoft.graph.downloadUrl")!=null?map.get("@microsoft.graph.downloadUrl").toString():null;
				String size = CommonUtil.getFormatSize(Double.parseDouble(map.get("size").toString()));
				Object folder = map.get("folder");
				Integer childCount =0;
				String t = null;
				String ext = null;
				String icon = null;
				String fileType = "folder";
				if(folder != null) {
					Map<String , Integer> folderMap = (Map<String , Integer>)folder;
					childCount = (Integer)folderMap.get("childCount");
				}else {
					fileType = ((String)JSONUtil.parse(map).getByPath("$.file.mimeType")).split("/")[0];
					ext = CommonUtil.fileType(name);
					icon = CommonUtil.fileIco(name);
					if(fileType.equals("audio")) {
						t = StrUtil.subBefore(thumbnail(tokenInfo, path, "large"), "&width=", true);
					}else{
						t = thumbnail(tokenInfo, path, "large");
					}
				}
				items.add(new Item(name, size, time, (folder==null?false:true), childCount ,downloadUrl, ext, icon, path.equals("/")?"/"+name:path+"/"+name, t, fileType));
			}
		}
		Object nextLink = JSONUtil.parse(body).getByPath("$.@odata.nextLink");
		if(nextLink != null) {
			 request.setUrl(nextLink.toString());
			 this.dirNextPage(items, request, 0, path, tokenInfo);
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
		if(StrUtil.isBlank(path) || path.equals("/")) {
			//根目录
		}else{
			path = String.format(":/%s:/", path);
		}
		return HttpRequest.get(Constants.apiUrl + "/me/drive/root" + path + query)
							.header("Authorization", tokenInfo.getAuth())
							.header("Host", "graph.microsoft.com")
							.timeout(Constants.timeout);
	}
	
}
