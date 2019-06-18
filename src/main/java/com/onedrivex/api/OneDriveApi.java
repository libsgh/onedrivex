package com.onedrivex.api;

import java.nio.charset.Charset;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import com.onedrivex.util.Constants;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;

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
		/*TokenInfo tokenInfo = new TokenInfo();
		tokenInfo.setAccess_token("eyJ0eXAiOiJKV1QiLCJub25jZSI6IkFRQUJBQUFBQUFEQ29NcGpKWHJ4VHE5Vkc5dGUtN0ZYelQtWnpGel9KMFk4MWFVdjYzLWltaExzdGhaSVhPMkxIZUVqamtKajdhRFNmRl9Ga2ZMcUpaTjRMNmhWTVczY21zWG1obXE1V005Qnd5dkxVb3ZYQUNBQSIsImFsZyI6IlJTMjU2IiwieDV0IjoiQ3RmUUM4TGUtOE5zQzdvQzJ6UWtacGNyZk9jIiwia2lkIjoiQ3RmUUM4TGUtOE5zQzdvQzJ6UWtacGNyZk9jIn0.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC80NGQ4NzlkYS1mM2VmLTQwMjQtODZlMy1hYmMyZTA3NmU2ODEvIiwiaWF0IjoxNTYwNDI2MTgzLCJuYmYiOjE1NjA0MjYxODMsImV4cCI6MTU2MDQzMDA4MywiYWNjdCI6MCwiYWNyIjoiMSIsImFpbyI6IjQyWmdZRWhzL2hsOElEWHVtbDJlUUt4eVVVdEd4NDNuUzM3YlYwYXhmT1k5SHN1bHNBOEEiLCJhbXIiOlsicHdkIl0sImFwcF9kaXNwbGF5bmFtZSI6Im9uZWluZGV4IiwiYXBwaWQiOiIwODMwYzNmZC02NWFiLTQyNDctOTE0NS04Y2E4ZTMyZWVhNDQiLCJhcHBpZGFjciI6IjEiLCJmYW1pbHlfbmFtZSI6ImlpIiwiZ2l2ZW5fbmFtZSI6ImNtIiwiaXBhZGRyIjoiMTEzLjIwOC4xMTIuMTMwIiwibmFtZSI6ImlpY20iLCJvaWQiOiJkNDE0NmRlNy1jNzI5LTQ5Y2YtYjI2Yy0yYTRiYzQ1N2U4ZTEiLCJwbGF0ZiI6IjMiLCJwdWlkIjoiMTAwMzNGRkZBRjUxNTg2MCIsInNjcCI6IkZpbGVzLlJlYWRXcml0ZSBVc2VyLlJlYWRXcml0ZSBwcm9maWxlIG9wZW5pZCBlbWFpbCIsInNpZ25pbl9zdGF0ZSI6WyJrbXNpIl0sInN1YiI6IjQwbnpxLWJEYkhnSEhva2ZCWFFscWR5TEJZbjB2SVJzenJKQTZJR3hrd1UiLCJ0aWQiOiI0NGQ4NzlkYS1mM2VmLTQwMjQtODZlMy1hYmMyZTA3NmU2ODEiLCJ1bmlxdWVfbmFtZSI6ImlpY21AbWFpbC5ocmthLm5ldCIsInVwbiI6ImlpY21AbWFpbC5ocmthLm5ldCIsInV0aSI6ImxCM0s5by1yMEVLeTRzeWN4aFJkQUEiLCJ2ZXIiOiIxLjAiLCJ4bXNfc3QiOnsic3ViIjoiVEhaN1g1MmM3VXcyQjJSOWxxMkxiemRndEEtWlpjRlY4UXN3NDRuT3ZUayJ9LCJ4bXNfdGNkdCI6MTUyNjEyNzI5OX0.At6Zj5Cef4Q9E1TLJWIaEPJQVNC-ypXe9BQ_D2rA1cVcJ7ifEJfQsnFQOQVHtZw0dayZJD5pRUkWkajlmpYwmkJLDKFKcr49DCshDxCvxGLQFD5nWAGfMCv3ioBgl6-yv-DlfMqyZEIr4CS5d3OEEBTAhZM8cFy-IZEr2Ay3lEkPvnTAysSPutceyqEwZHGaBstMRzU4bf_sz-ZGdjAHw3oRHH9jdNah7sB72-qIoch2NdmlQsEu58E1eSY-oPP5Ichd8zO4Zz1IdKSuUUVGNW2hvpZ9tWbLIwquikACWF3cAwDi4RMaBXNrHTB4uOlH00Bt2S3hch541V89G9Lp3Q\",\"refresh_token\":\"OAQABAAAAAADCoMpjJXrxTq9VG9te-7FX6rsRHVr7hpeC0oIAxbtDGFcskRXub8qLZ_VhcBw72iSXcUAtcGcHzJOZjCW9VEjWUUaXsRP-4QLnSqKVm6JMyGoo-FWZmkBxO6vz4q0Cm2n9Uuf7UW11fA9ngg6kizsG29e0J9ppMOBYcXsHwmYj7TJsEqIj_bjJdv24-P1zllSVRKds9G31AVX2C7PlVvrTlsPFt9KgWq9vQDh0PSP9t84qUEjDt6O4N4qJbXQ5yKad6wbr2mb1ELkqw_qlNOFLX-dthS0Q0iIvPxLUE_TIYz1tPT8cBRuN2VFs_S6U9ZGl2w1h9k1C74Sz6JvcJGL4IIEcPhpTLZ2xr5zsAjTcm6oeeukR8NA86YvRIDexsNM8SJF2hL5_5WNNI41OErv-mPiuisMtKVCQPBPA0aq2nC8WjOerTJ4biAhH3xI5NAJw_gKh7-KAel95M5KqACCFZ37_2y9wtxdBt1D0lFblZBah6rPoWfOeBe3iWzE1Kdm0SyJ0n0rRFqcK-B6dbcrRTOFxxeTaMb7j067q4vCjKqB0xZHOg8xVxmQSsVyeFjc2FI-FRYl7BR4wH9RXgTi3b90fWCw5Yhf_9ItzzRTtAIdMTlMcAma4qNkRQSwmDzwhzbZF8O5b_g36cAHetQ2McuvuVq3W7ohXWMbl654Gb5zv14VHwpxsYmwjXgnDtWn-QMzancMEvvN7rExvO4i-gk2hZiJkZDL_t9jUKBBCfiIbaLLsufr4tmKmP9-f4U_V273mMT2UC0TQCeHlh5CJpckRlUN1DvanfI8WvAsEeCAA");
		tokenInfo.setToken_type("Bearer");*/
		//System.out.println(oneDrive.refreshToken("OAQABAAAAAADCoMpjJXrxTq9VG9te-7FXi2Kc-Ydof2AF5c92T8YG6v9fGTc1hqhbTYtqp362azKxKxoR8_Llr1QWYQ5VuZVs7Ks9dBwWgP2EsVxtfxVa-VbqARATbkkmnqtqCkw-NXb5TxN4GNuc_9qZF8oDwBFRcIDSKTg-_Z590piNDrnmD1JsGbEhCYUIDqePj2xPBe07Cj9IhbW0q7SzK6SXIUlF2jKk2fHHEsXOFshqeVlfolLCIr8ONDM1Amfd9SfskbdIIt_WG9TzE4thK8PNcr4Em2DRqe7-odPZb8eGTZ4SaZN7fzqBtCOvE5kpx0dimCuXZ8XZ_Ns1e4_-slmB2TjrJvgrm2f-q8NVaU6zmq-fhhKkLxLZdO--SsNu0RGY4y5THzbEBSyIOc4Eef1w_OSt_J1uzJlQgxcA7VFumVMFzA5WT_a0RqXgFLSykDoIFqCAAgjjnXxEsDrT9v3lnOOxbDCCUUuDTBDK-vXn408pByvLNjYOshK4ihL5Eu7C6LG_Kuac7OdVDQv8ucupMqeKe1hwqVkEnhNfLiyPE33aW0avGVLvv6WQRUvnVTcyw8yI3nvWIQZ4cTsB0VFV47uPyNHDuMP4M0Z_lZE_ZdMc9_OZSzzPmaj6NXhAaI7-MFe_JOuXDtTyGLUy6eyiFge8I4icdo4PcYTBSpjfo4_PM52AUCiVaGKLxKEhhWCx8BA-6MC9GuxxdslsIUKr6OiyccxF2XvXQPEf7dJgYMoGq8V_EJVy8IHiTX_kCwRqQ_BU9aScgU4Zb45ltWim3WXsuPpa-tIM3IJnGvtaZ--7-yAA", "0830c3fd-65ab-4247-9145-8ca8e32eea44", "mdeeMXTEIQT0981=(!gme0{", "http://localhost:8080/authRedirect"));
		String deepLink = "/quickstart/graphIO?publicClientSupport=false&appName=onedrivex";
		System.out.println(URLUtil.encodeAll(deepLink, Charset.forName("utf-8")));
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
	 * 获取根目录列表
	 * @param tokenInfo
	 * @return
	 */
	public String getRootDir(TokenInfo tokenInfo) {
		return HttpRequest.get(Constants.apiUrl+"/drive/root/children?select=name,size,folder,@microsoft.graph.downloadUrl,lastModifiedDateTime")
					.header("Authorization", tokenInfo.getAuth())
					.header("Host", "graph.microsoft.com")
					.timeout(Constants.timeout)
					.execute().body();
	}
	
}
