package com.onedrivex.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.onedrivex.api.OneDriveApi;
import com.onedrivex.api.TokenInfo;
import com.onedrivex.service.DbService;
import com.onedrivex.util.Constants;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

@Controller
public class IndexController {
	
	private OneDriveApi api = OneDriveApi.getInstance();
	
	@Autowired
	private DbService servive;

	@RequestMapping("/")
	public String index(Model model) {
		String tokenInfo = servive.getConfig(Constants.tokenKey);
		if(StrUtil.isBlank(tokenInfo)) {
			return "redirect:/setup?s=1";
		}else{
			TokenInfo ti = JSONUtil.toBean(tokenInfo, TokenInfo.class);
			model.addAttribute("test", api.getRootDir(ti));
		}
		return "index";
	}
	@RequestMapping("/setup")
	public String setup(String s, Model model, String clientId, String clientSecret, String redirectUri) {
		if(s.equals("1")) {
			String rdu = servive.getConfig("redirectUri");
			model.addAttribute("appUrl", api.quickStartRegUrl(rdu));
			model.addAttribute("redirectUri", rdu);
			return "setup_1";
		}else if(s.equals("2")) {
			servive.updateConfig("redirectUri",redirectUri);
			servive.updateConfig("clientId",clientId);
			servive.updateConfig("clientSecret",clientSecret);
			model.addAttribute("oauth2Url",api.oauth2(clientId, redirectUri));
			return "setup_2";
		}
		return "setup_1";
	}
	
	@RequestMapping("/authRedirect")
	public String authRedirect(String code) {
		if(StrUtil.isBlank(code)) {
			return "参数不正确";
		}
		Map<String, String> configMap = servive.getConfigMap();
		String clientId = configMap.get("clientId");
    	String clientSecret = configMap.get("clientSecret");
    	String redirectUri = configMap.get("redirectUri");
		String tokenInfo = api.getToken(code, clientId, clientSecret, redirectUri);
		servive.updateConfig(Constants.tokenKey, tokenInfo);
		return "redirect:/";
	}
}
