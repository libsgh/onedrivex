package com.onedrivex.api;

/**
 * 令牌信息
 * @author libs
 *
 */
public class TokenInfo {
	
	private String token_type;//token类型：Bearer
	
	private String scope;//权限范围：Files.ReadWrite profile openid email
	
	//访问令牌仅对expires_in属性中指定的秒数有效
	private Integer expires_in;//令牌过期时间（单位秒）：3600
	
	private Integer ext_expires_in;//令牌过期时间（秒）：3600
	
	private String access_token;//访问令牌
	
	private String refresh_token;//刷新令牌

	public String getToken_type() {
		return token_type;
	}

	public void setToken_type(String token_type) {
		this.token_type = token_type;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public Integer getExpires_in() {
		return expires_in;
	}

	public void setExpires_in(Integer expires_in) {
		this.expires_in = expires_in;
	}

	public Integer getExt_expires_in() {
		return ext_expires_in;
	}

	public void setExt_expires_in(Integer ext_expires_in) {
		this.ext_expires_in = ext_expires_in;
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public String getRefresh_token() {
		return refresh_token;
	}

	public void setRefresh_token(String refresh_token) {
		this.refresh_token = refresh_token;
	}

	@Override
	public String toString() {
		return "TokenInfo [token_type=" + token_type + ", scope=" + scope + ", expires_in=" + expires_in
				+ ", ext_expires_in=" + ext_expires_in + ", access_token=" + access_token + ", refresh_token="
				+ refresh_token + "]";
	}

	public String getAuth() {
		return token_type+" "+access_token;
	}
	
}
