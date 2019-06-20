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
		tokenInfo.setAccess_token("eyJ0eXAiOiJKV1QiLCJub25jZSI6IkFRQUJBQUFBQUFEQ29NcGpKWHJ4VHE5Vkc5dGUtN0ZYQmU0YU5qS3Y3QUJ2ck9Wb3F1XzVXWUpmUm9zTzBSMzExM016RDdLZUZDYV9IN1BBMkdGNUdIeTNiay11SldOeHVOWmw0clV6YWV0d1p4ejFySnZDM2lBQSIsImFsZyI6IlJTMjU2IiwieDV0IjoiQ3RmUUM4TGUtOE5zQzdvQzJ6UWtacGNyZk9jIiwia2lkIjoiQ3RmUUM4TGUtOE5zQzdvQzJ6UWtacGNyZk9jIn0.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC80NGQ4NzlkYS1mM2VmLTQwMjQtODZlMy1hYmMyZTA3NmU2ODEvIiwiaWF0IjoxNTYxMDEyOTgyLCJuYmYiOjE1NjEwMTI5ODIsImV4cCI6MTU2MTAxNjg4MiwiYWNjdCI6MCwiYWNyIjoiMSIsImFpbyI6IkFTUUEyLzhMQUFBQXpvbFdPazBRMXNKbStWVzBxZFZGcUoza2tvN1BNcUxiOThJcVV2ZTFvOVE9IiwiYW1yIjpbInB3ZCJdLCJhcHBfZGlzcGxheW5hbWUiOiJvbmVkcml2ZXgiLCJhcHBpZCI6IjQwYWU2OWU3LWI3ZDgtNGYyZi04ZDYwLTA4MzFjOWJmOWQwNCIsImFwcGlkYWNyIjoiMSIsImZhbWlseV9uYW1lIjoiaWkiLCJnaXZlbl9uYW1lIjoiY20iLCJpcGFkZHIiOiIxMTQuMjQ0LjM2LjEzMCIsIm5hbWUiOiJpaWNtIiwib2lkIjoiZDQxNDZkZTctYzcyOS00OWNmLWIyNmMtMmE0YmM0NTdlOGUxIiwicGxhdGYiOiIzIiwicHVpZCI6IjEwMDMzRkZGQUY1MTU4NjAiLCJzY3AiOiJGaWxlcy5SZWFkV3JpdGUuQWxsIHByb2ZpbGUgb3BlbmlkIGVtYWlsIiwic2lnbmluX3N0YXRlIjpbImttc2kiXSwic3ViIjoiNDBuenEtYkRiSGdISG9rZkJYUWxxZHlMQlluMHZJUnN6ckpBNklHeGt3VSIsInRpZCI6IjQ0ZDg3OWRhLWYzZWYtNDAyNC04NmUzLWFiYzJlMDc2ZTY4MSIsInVuaXF1ZV9uYW1lIjoiaWljbUBtYWlsLmhya2EubmV0IiwidXBuIjoiaWljbUBtYWlsLmhya2EubmV0IiwidXRpIjoiS1U4UU5YY1R6RUt6anFUWVlQRnNBQSIsInZlciI6IjEuMCIsInhtc19zdCI6eyJzdWIiOiJJd1ZnZ1kzSC04TjBsNU1BR3NyNUprRVJhakM1M2s0S09XMUpNdmdrWDNFIn0sInhtc190Y2R0IjoxNTI2MTI3Mjk5fQ.O_uLlDLIPrSvIBjOw0zY9U_5dH2Fr2ienZ9iqcT0M8DADXLvCCE99-gUxmG0dgaEOoNfKe-G41vz9B1XmMC1IOQuBpvhrlL1u-Vo_hbiISqx2p4bngIGr1PsX2HwbHGFb3DQKnEshcuSuNF_Unr71PDwOUHwLrI3M0bWWX8yU_2zUWCHp235n64OmTnLRZoLNFuBuRQMgUPffK4TpDYhr4t4R-GYgB3nSxK4FVLFi56CWPH6d9VsWXGN0Ar2IkXJC4w_gMRuZ_zcYIoXhLFvJGU2GQpYhHVGmJO2RC2e6GdsNczbJqfUPn6cHzqK2083q9eCWfE8nT8bdxluCR5y0w");
		oneDrive.getDir(tokenInfo, "/doc/大数据");
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
		HttpRequest request = request(path, "children?select=name,size,folder,@microsoft.graph.downloadUrl,lastModifiedDateTime", tokenInfo);
		List<Item> items = new ArrayList<Item>();
		this.dirNextPage(items, request, 0, path);
		return items;
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
				items.add(new Item(name, size, time, (folder==null?false:true), childCount ,downloadUrl, ext, path.equals("/")?"/"+name:path+"/"+name));
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
