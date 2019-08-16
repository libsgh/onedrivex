package com.onedrivex.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.onedrivex.util.CommonUtil;
import com.onedrivex.util.Constants;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
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
	
	public String oauth2(String cliendId, String redirectUri, String host, String state) {
		if(!host.equals("localhost")) {
			return String.format("https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id=%s&scope=%s&response_type=code&redirect_uri=%s&state=%s", cliendId, Constants.scope, URLUtil.encode(redirectUri), URLUtil.encode(state));
		}else {
			return String.format("https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id=%s&scope=%s&response_type=code&redirect_uri=%s", cliendId, Constants.scope, URLUtil.encode(redirectUri));
		}
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
		String size = CommonUtil.getFormatSize(Double.parseDouble(json.getStr("size")));
		Double fileSize = Double.parseDouble(json.getStr("size"));
		String icon = null;
		String id = json.getStr("id");
		String ext = null;
		Boolean folder = json.get("folder")==null?false:true;
		Integer childCount = 0;
		String t = null;
		String fileType = "folder";
		if(folder) {
			@SuppressWarnings("unchecked")
			Map<String , Integer> folderMap = (Map<String , Integer>)json.get("folder");
			childCount = (Integer)folderMap.get("childCount");
		}else {
			fileType = ((String)json.getByPath("$.file.mimeType")).split("/")[0];
			ext = CommonUtil.fileType(name);
			icon = CommonUtil.fileIco(name);
		/*	if(fileType.equals("audio")) {
				t = StrUtil.subBefore(thumbnail(tokenInfo, path, "large"), "&width=", true);
			}else{
				t = thumbnail(tokenInfo, path, "large");
			}*/
		}
		//String t = thumbnail(tokenInfo, path, "large");
		return new Item(id, name, size, time, folder, childCount ,downloadUrl, ext, icon, path, t, fileType, fileSize);
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
				String id = map.get("id").toString();
				String downloadUrl = map.get("@microsoft.graph.downloadUrl")!=null?map.get("@microsoft.graph.downloadUrl").toString():null;
				String size = CommonUtil.getFormatSize(Double.parseDouble(map.get("size").toString()));
				Double fileSize = Double.parseDouble(map.get("size").toString());
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
					/*if(fileType.equals("audio")) {
						t = StrUtil.subBefore(thumbnail(tokenInfo, path, "large"), "&width=", true);
					}else{
						t = thumbnail(tokenInfo, path, "large");
					}*/
				}
				items.add(new Item(id, name, size, time, (folder==null?false:true), childCount ,downloadUrl, ext, icon, path.equals("/")?"/"+name:path+"/"+name, t, fileType, fileSize));
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
	
	/**
	 * 创建上传会话，获取上传url
	 * @param path
	 * @param tokenInfo
	 * @return
	 */
	public String createUploadSession(String path, TokenInfo tokenInfo) {
		HttpResponse rep = HttpRequest.post(Constants.apiUrl + "/me/drive/root:"+path+":/createUploadSession")
							.header("Authorization", tokenInfo.getAuth())
							.header("Host", "graph.microsoft.com")
							.timeout(Constants.timeout)
							.contentType("Content-Type")
							.execute();
		if(rep.getStatus() == 409) {
			return null;
		}else {
			JSONObject json = JSONUtil.parseObj(rep.body());
			return json.getStr("uploadUrl");
		}
	}
	
	/**
	 * 取消上传会话
	 * @param uploadUrl
	 * @return
	 */
	public Boolean delUploadSession(String uploadUrl) {
		HttpResponse rep = HttpRequest.delete(uploadUrl).execute();
		if(rep.getStatus() == 204) {
			return true;
		}
		return false;
	}
	
	/**
	 * 删除文件
	 * @param itemId
	 * @return
	 */
	public Boolean delItem(String itemId, TokenInfo tokenInfo) {
		HttpResponse rep = HttpRequest.delete(Constants.apiUrl + "/me/drive/items/"+itemId)
				.header("Authorization", tokenInfo.getAuth())
				.header("Host", "graph.microsoft.com")
				.timeout(Constants.timeout)
				.execute();
		if(rep.getStatus() == 204) {
			return true;
		}
		return false;
	}
	
	/**
	 * 获取上传状态
	 * @param uploadUrl
	 * @return
	 */
	public String getUploadStatus(String uploadUrl) {
		String body = HttpRequest.get(uploadUrl).execute().body();
		return body;
	}
	
	/**
	 * 上传文件
	 * @param path
	 * @param tokenInfo
	 * @return
	 */
	public JSONObject upload(UploadInfo uploadInfo, String uploadUrl, TokenInfo tokenInfo, long totalSize) {
		String body = HttpRequest.put(uploadUrl)
				.header("Content-Length", totalSize+"")
				.header("Content-Range","bytes "+uploadInfo.getBegin()+"-"+uploadInfo.getEnd()+"/"+totalSize)
				.body(uploadInfo.getBytes())
				.execute().body();
		return JSONUtil.parseObj(body);
	}

	/*public static void main(String[] args) {
		OneDriveApi oneDrive = new OneDriveApi();
		TokenInfo tokenInfo = new TokenInfo();
		tokenInfo.setToken_type("Bearer");
		tokenInfo.setAccess_token("eyJ0eXAiOiJKV1QiLCJub25jZSI6IkFRQUJBQUFBQUFBUDB3TGxxZExWVG9PcEE0a3d6U254d2wtWFBOQjZ6VjlYOEthUHgxSkdoTXF4bGxWNnpKSDZQcUktdXNLOThmTWZIVnZQYmNJOU4tVnRSVHBGVndHemFaVHExVFBxOWstSUlxaXdFWnRFM1NBQSIsImFsZyI6IlJTMjU2IiwieDV0IjoidTRPZk5GUEh3RUJvc0hqdHJhdU9iVjg0TG5ZIiwia2lkIjoidTRPZk5GUEh3RUJvc0hqdHJhdU9iVjg0TG5ZIn0.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC80NGQ4NzlkYS1mM2VmLTQwMjQtODZlMy1hYmMyZTA3NmU2ODEvIiwiaWF0IjoxNTYzMjY4MjAwLCJuYmYiOjE1NjMyNjgyMDAsImV4cCI6MTU2MzI3MjEwMCwiYWNjdCI6MCwiYWNyIjoiMSIsImFpbyI6IkFTUUEyLzhNQUFBQWFQb1UrVTdQbWxibE1zdmdYekJ3NytYNWRSTDNkSFBiS3UydXN2eTg4VTA9IiwiYW1yIjpbInB3ZCJdLCJhcHBfZGlzcGxheW5hbWUiOiJvbmVkcml2ZXgiLCJhcHBpZCI6IjQwYWU2OWU3LWI3ZDgtNGYyZi04ZDYwLTA4MzFjOWJmOWQwNCIsImFwcGlkYWNyIjoiMSIsImZhbWlseV9uYW1lIjoiaWkiLCJnaXZlbl9uYW1lIjoiY20iLCJpcGFkZHIiOiIxMTQuMjQ0LjM2LjEzMCIsIm5hbWUiOiJpaWNtIiwib2lkIjoiZDQxNDZkZTctYzcyOS00OWNmLWIyNmMtMmE0YmM0NTdlOGUxIiwicGxhdGYiOiIzIiwicHVpZCI6IjEwMDMzRkZGQUY1MTU4NjAiLCJzY3AiOiJGaWxlcy5SZWFkV3JpdGUuQWxsIHByb2ZpbGUgb3BlbmlkIGVtYWlsIiwic2lnbmluX3N0YXRlIjpbImttc2kiXSwic3ViIjoiNDBuenEtYkRiSGdISG9rZkJYUWxxZHlMQlluMHZJUnN6ckpBNklHeGt3VSIsInRpZCI6IjQ0ZDg3OWRhLWYzZWYtNDAyNC04NmUzLWFiYzJlMDc2ZTY4MSIsInVuaXF1ZV9uYW1lIjoiaWljbUBtYWlsLmhya2EubmV0IiwidXBuIjoiaWljbUBtYWlsLmhya2EubmV0IiwidXRpIjoicTMxNk9LcEtoVTZzMVBJM3BqcDRBQSIsInZlciI6IjEuMCIsInhtc19zdCI6eyJzdWIiOiJJd1ZnZ1kzSC04TjBsNU1BR3NyNUprRVJhakM1M2s0S09XMUpNdmdrWDNFIn0sInhtc190Y2R0IjoxNTI2MTI3Mjk5fQ.oBERAib51nIEpfXaOFxj5OuFIjVQ-998P6HWVq4oBHeM-b1UqIB-7Vk-rBTQsWAcNvnSqVlYycN3DCJGPHhnqVwVGAGSICGotqw5m8udFPsGyo-sMHRn9Z6UNLhH9KRcjffufZjd1UTa1sgNrwMYRHTGM7HP0aX-Wh6-zNY_IodyodfPMWrYZ31dJ98gnCaVEKLGcLkK20wFJMnRgD_kWBM5urDjAAbuM5dqJj9l0UvIyX7JPJxSW7b12iN26Jt-rAA_w96EAqKHOIka792jV3BeJCM40lVN1LDoP18-WOKFs3S46qTkgeDa4VjfrUB4mayAW6K_O6F_Zjkg68IF9A");
		//System.out.println(oneDrive.getFile(tokenInfo, "/test").toString());
		File file = new File("D:\\data\\split\\Instagram_v101.0.0.15.120_apkpure.com.apk");
		SplitFile sc = new SplitFile(file, Constants.splitFileSize);//15.625MB
	    sc.init();
	    List<UploadInfo> list = sc.spiltfile("D:\\data\\split\\");
	    String upLoadUrl = oneDrive.createUploadSession("/test"+"/"+file.getName(), tokenInfo);
	    long length = FileUtil.size(file);
	    System.out.println(length);
	    for (UploadInfo uploadInfo : list) {
	    	System.out.println(oneDrive.upload(uploadInfo, upLoadUrl, tokenInfo, length));
	    	System.out.println(oneDrive.getUploadStatus(upLoadUrl));
		}
	}*/
}
