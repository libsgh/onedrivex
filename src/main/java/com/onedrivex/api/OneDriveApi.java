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
		tokenInfo.setAccess_token("eyJ0eXAiOiJKV1QiLCJub25jZSI6IkFRQUJBQUFBQUFEQ29NcGpKWHJ4VHE5Vkc5dGUtN0ZYNFVPekZWMkNldDN4N3pMcmtMcFNGLThrbF9HMGo5NlF2WGdtd1hYTTVKOWhtYjN6Z0k5aWZJZnRCcG5uMmpMdFFaUWhQZzN4QVphZjBCQk1oUDY3QXlBQSIsImFsZyI6IlJTMjU2IiwieDV0IjoiQ3RmUUM4TGUtOE5zQzdvQzJ6UWtacGNyZk9jIiwia2lkIjoiQ3RmUUM4TGUtOE5zQzdvQzJ6UWtacGNyZk9jIn0.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC80NGQ4NzlkYS1mM2VmLTQwMjQtODZlMy1hYmMyZTA3NmU2ODEvIiwiaWF0IjoxNTYxMzQxMTM1LCJuYmYiOjE1NjEzNDExMzUsImV4cCI6MTU2MTM0NTAzNSwiYWNjdCI6MCwiYWNyIjoiMSIsImFpbyI6IkFTUUEyLzhMQUFBQTBnT0JlTkJpbjdXLzE5T0VtOCtFanR5a1RXVTJNTzZuTkkzdXk0SG5JZUk9IiwiYW1yIjpbInB3ZCJdLCJhcHBfZGlzcGxheW5hbWUiOiJvbmVkcml2ZXgiLCJhcHBpZCI6IjIzNWMyZDIzLTMwYmQtNDllZS1hZWZjLWRhYzVjMjE3MTFhNCIsImFwcGlkYWNyIjoiMSIsImZhbWlseV9uYW1lIjoiaWkiLCJnaXZlbl9uYW1lIjoiY20iLCJpcGFkZHIiOiIxMTMuMjA4LjExMi4xMzAiLCJuYW1lIjoiaWljbSIsIm9pZCI6ImQ0MTQ2ZGU3LWM3MjktNDljZi1iMjZjLTJhNGJjNDU3ZThlMSIsInBsYXRmIjoiMyIsInB1aWQiOiIxMDAzM0ZGRkFGNTE1ODYwIiwic2NwIjoiRmlsZXMuUmVhZFdyaXRlLkFsbCBwcm9maWxlIG9wZW5pZCBlbWFpbCIsInNpZ25pbl9zdGF0ZSI6WyJrbXNpIl0sInN1YiI6IjQwbnpxLWJEYkhnSEhva2ZCWFFscWR5TEJZbjB2SVJzenJKQTZJR3hrd1UiLCJ0aWQiOiI0NGQ4NzlkYS1mM2VmLTQwMjQtODZlMy1hYmMyZTA3NmU2ODEiLCJ1bmlxdWVfbmFtZSI6ImlpY21AbWFpbC5ocmthLm5ldCIsInVwbiI6ImlpY21AbWFpbC5ocmthLm5ldCIsInV0aSI6InAtdWVqNHUzXzBLN3hvb0UzRFFuQUEiLCJ2ZXIiOiIxLjAiLCJ4bXNfc3QiOnsic3ViIjoiZ25qRnMtdDZuQkl4YjdHRkY0R2VGMW02dTA2ellTN2JIMEhWVlU2MGdOVSJ9LCJ4bXNfdGNkdCI6MTUyNjEyNzI5OX0.M_O0Vn_Ob04EA0MFF-4R7SUfaNX_hKM7unscU9YVvfeWtxRT41sjJLH-7JAoO4bmdvlvx_Xv65y_z7gP4qY3AntyNKCOnOeIxlWOUpLO2MuWTiFaA92CETkLdy5otAekPbLspdRg1-fWpQHITrYnUl4LmDgTvIaoXxMpaaie1OQ1fT0kRS4Be1g0ljtAoQuFe8qZ-eY2x9H40cxVJM-F51ptMoGdHTQCA1RBiEUYs-jnx60izr1Ug9DuhWj3rJQaUBSYaoNmtJOWEI23syE-X_VW6EfP9BLPYCJ7ZG-irx-88icZA9oz_oVXMlPTLp2qNoUWzaDujex0fI8aEu3dsA");
		//System.out.println(oneDrive.getFile(tokenInfo, "/image/1989362.jpg").toString());
		System.out.println(oneDrive.getFile(tokenInfo, "/video/蚁人.mkv").toString());
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
		HttpRequest request = request(path, "children?select=id,name,size,folder,@microsoft.graph.downloadUrl,lastModifiedDateTime", tokenInfo);
		List<Item> items = new ArrayList<Item>();
		this.dirNextPage(items, request, 0, path);
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
		String ext = null;
		String fileType = ((String)json.getByPath("$.file.mimeType")).split("/")[0];
		Boolean folder = json.get("folder")==null?false:true;
		Integer childCount = 0;
		if(folder) {
			Map<String , Integer> folderMap = (Map<String , Integer>)json.get("folder");
			childCount = (Integer)folderMap.get("childCount");
		}else {
			ext = CommonUtil.fileIco(name);
		}
		//String t = StrUtil.subBefore(thumbnail(tokenInfo, path, "large"), "&width=", true);
		String t = thumbnail(tokenInfo, path, "large");
		return new Item(name, size, time, folder, childCount ,downloadUrl, ext, path, t, fileType);
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
	public void dirNextPage(List<Item> items, HttpRequest request, int retry, String path) {
		String body = request.execute().body();
		if(StrUtil.isBlank(body) && retry < 3) {
			retry++;
			this.dirNextPage(items, request, retry, path);
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
				String ext = null;
				if(folder != null) {
					Map<String , Integer> folderMap = (Map<String , Integer>)folder;
					childCount = (Integer)folderMap.get("childCount");
				}else {
					ext = CommonUtil.fileIco(name);
				}
				items.add(new Item(name, size, time, (folder==null?false:true), childCount ,downloadUrl, ext, path.equals("/")?"/"+name:path+"/"+name, null, null));
			}
		}
		Object nextLink = JSONUtil.parse(body).getByPath("$.@odata.nextLink");
		if(nextLink != null) {
			 request.setUrl(nextLink.toString());
			 this.dirNextPage(items, request, 0, path);
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
