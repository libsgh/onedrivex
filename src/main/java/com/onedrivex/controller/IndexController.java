package com.onedrivex.controller;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.onedrivex.api.Item;
import com.onedrivex.api.OneDriveApi;
import com.onedrivex.api.TokenInfo;
import com.onedrivex.common.CommonUtil;
import com.onedrivex.service.XService;
import com.onedrivex.util.Constants;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;

@Controller
@ControllerAdvice
public class IndexController {
	
	private OneDriveApi api = OneDriveApi.getInstance();
	
	@Autowired
	private XService servive;
	
	@ModelAttribute("title")
	public String getTitle() {
		return servive.getConfig("title");
	}
	
	@RequestMapping("/**")
	public String index(Model model, HttpServletRequest request) {
		String parentPath = CommonUtil.getParentPath(request.getRequestURI());
		String path = URLUtil.decode(request.getRequestURI());
		String tokenInfo = servive.getConfig(Constants.tokenKey);
		model.addAttribute("parentPath", parentPath);
		model.addAttribute("allPaths", CommonUtil.getAllPaths(request.getRequestURI()));
		if(StrUtil.isBlank(tokenInfo)) {
			return "redirect:/setup?s=1";
		}else{
			TokenInfo ti = JSONUtil.toBean(tokenInfo, TokenInfo.class);
			Item item = servive.getFile(ti, path);
			if(item == null || item.getFolder()) {
				model.addAttribute("items", servive.getDir(ti, path));
			}else{
				//文件
				String sfs = request.getHeader("Referer");
				if(StrUtil.isNotBlank(sfs)) {
					model.addAttribute("item", item);
					return "nexmoe/show/image";
				}else{
					//下载
					return "redirect:"+item.getDownloadUrl();
				}
			}
		}
		return "nexmoe/list";
	}
	@RequestMapping("/setup")
	public String setup(String s, Model model, String clientId, String clientSecret, String redirectUri) {
		if(s.equals("1")) {
			String rdu = servive.getConfig("redirectUri");
			model.addAttribute("appUrl", api.quickStartRegUrl(rdu));
			model.addAttribute("redirectUri", rdu);
			return "classic/setup/setup_1";
		}else if(s.equals("2")) {
			servive.updateConfig("redirectUri",redirectUri);
			servive.updateConfig("clientId",clientId);
			servive.updateConfig("clientSecret",clientSecret);
			model.addAttribute("oauth2Url",api.oauth2(clientId, redirectUri));
			return "setup/setup_2";
		}
		return "classic/setup/setup_1";
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
