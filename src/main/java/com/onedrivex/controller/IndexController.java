package com.onedrivex.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import freemarker.template.utility.StringUtil;

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
	
	public static String getUrl(HttpServletRequest request) {
		String url = "";
		if(request.getServerPort()== 443 ||request.getServerPort() == 80) {
			url = request.getScheme() +"://" + request.getServerName()
			+ request.getServletPath();
		}else {
			url = request.getScheme() +"://" + request.getServerName()  
			+ ":" +request.getServerPort() 
			+ request.getServletPath();
		}
	    return url;
	}
    
	@RequestMapping("/**")
	public String index(Model model, HttpServletRequest request, Integer t, String password) {
		String parentPath = CommonUtil.getParentPath(request.getRequestURI());
		String path = URLUtil.decode(request.getRequestURI());
		String tokenInfo = servive.getConfig(Constants.tokenKey);
		model.addAttribute("parentPath", parentPath);
		model.addAttribute("allPaths", CommonUtil.getAllPaths(request.getRequestURI()));
		model.addAttribute("realUrl", getUrl(request));
		if(StrUtil.isBlank(tokenInfo)) {
			return "redirect:/setup?s=1";
		}else{
			TokenInfo ti = JSONUtil.toBean(tokenInfo, TokenInfo.class);
			Item item = servive.getFile(ti, path);
			if(item == null || item.getFolder()) {
				List<Item> items = servive.getDir(ti, path);
				int count = items.size();
				items = items.parallelStream().filter(r->!r.getName().trim().equals(".password")).collect(Collectors.toList());
				if(count > items.size()) {
					String pwd = HttpUtil.downloadString(servive.getFile(ti, path+"/.password").getDownloadUrl(), "UTF-8");
					//需要密码
					if(StrUtil.isBlank(password) || !password.trim().equals(pwd.trim())) {
						return "nexmoe/password";
					}
				}
				model.addAttribute("items", items);
			}else{
				//文件
				String sfs = request.getHeader("Referer");
				if(StrUtil.isNotBlank(sfs)) {
					model.addAttribute("item", item);
					return CommonUtil.showORedirect(model, item, "nexmoe", ti, t);
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
			//获取并输入cliendId secret
			String rdu = servive.getConfig("redirectUri");
			model.addAttribute("appUrl", api.quickStartRegUrl(rdu));
			model.addAttribute("redirectUri", rdu);
			return "classic/setup/setup_1";
		}else if(s.equals("2")) {
			//oauth2跳转授权
			servive.updateConfig("redirectUri",redirectUri);
			servive.updateConfig("clientId",clientId);
			servive.updateConfig("clientSecret",clientSecret);
			model.addAttribute("oauth2Url",api.oauth2(clientId, redirectUri));
			return "classic/setup/setup_2";
		}else if(s.equals("3")) {
			//安装结果
			String tokenInfo = servive.getConfig(Constants.tokenKey);
			TokenInfo ti = JSONUtil.toBean(tokenInfo, TokenInfo.class);
			model.addAttribute("ti", ti);
			return "classic/setup/setup_3";
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
		return "redirect:/setup?s=3";
	}
}
