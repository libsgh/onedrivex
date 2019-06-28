package com.onedrivex.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

@Controller
@ControllerAdvice
public class IndexController {
	
	private OneDriveApi api = OneDriveApi.getInstance();
	
	@Autowired
	private XService servive;
	
	@ModelAttribute("siteName")
	public String getSiteName() {
		return servive.getConfig("siteName");
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
	public String index(Model model, HttpServletRequest request, HttpServletResponse response, Integer t, String password) {
		String parentPath = CommonUtil.getParentPath(request.getRequestURI());
		String path = URLUtil.decode(request.getRequestURI());
		String tokenInfo = servive.getConfig(Constants.tokenKey);
		model.addAttribute("parentPath", parentPath);
		model.addAttribute("allPaths", CommonUtil.getAllPaths(request.getRequestURI()));
		model.addAttribute("realUrl", getUrl(request));
		model.addAttribute("path", path);
		if(StrUtil.isBlank(tokenInfo)) {
			return "redirect:/setup?s=1";
		}else{
			TokenInfo ti = JSONUtil.toBean(tokenInfo, TokenInfo.class);
			Item item = servive.getFile(ti, path);
			if((item != null && item.getFolder())||path.equals("/")) {
				List<Item> items = servive.getDir(ti, path);
				int count = items.size();
				items = items.parallelStream().filter(r->!r.getName().trim().equals(".password")).collect(Collectors.toList());
				if(count > items.size()) {
					String pwd = HttpUtil.downloadString(servive.getFile(ti, path+"/.password").getDownloadUrl(), "UTF-8");
					String pwdMd5 = CommonUtil.getCookie(request, path.replaceAll("/", ""));
					if(StrUtil.isBlank(pwdMd5) || !pwdMd5.equals(SecureUtil.md5(pwd))) {
						//密码cookie不存在
						//1.重定向到密码输入页面
						//2.密码提交-》写入cookie
						if(StrUtil.isBlank(password) || !password.trim().equals(pwd.trim())) {
							return Constants.globalConfig.get("theme")+"/password";
						}else{
							//cookie写入密码
							Cookie cookie = new Cookie(path.replaceAll("/", ""), SecureUtil.md5(pwd));
							cookie.setMaxAge(30*24*60*60);
							response.addCookie(cookie);
						}
					}
				}
				int countList = items.size();
				items = items.parallelStream().filter(r->!r.getName().trim().equals("README.md")).collect(Collectors.toList());
				if(countList > items.size()) {
					String readme = HttpUtil.downloadString(servive.getFile(ti, path+"/README.md").getDownloadUrl(), "UTF-8");
					model.addAttribute("readme", readme);
				}
				model.addAttribute("items", items);
			}else if(item != null && !item.getFolder()){
				//文件
				String sfs = request.getHeader("Referer");
				if(StrUtil.isNotBlank(sfs)) {
					model.addAttribute("item", item);
					return CommonUtil.showORedirect(model, item, Constants.globalConfig.get("theme"), ti, t);
				}else{
					//下载
					return "redirect:"+item.getDownloadUrl();
				}
			}else {
				return Constants.globalConfig.get("theme")+"/404";
			}
		}
		return Constants.globalConfig.get("theme")+"/list";
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
