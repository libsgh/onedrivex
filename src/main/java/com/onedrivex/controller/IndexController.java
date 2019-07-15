package com.onedrivex.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.onedrivex.api.Item;
import com.onedrivex.api.OneDriveApi;
import com.onedrivex.api.TokenInfo;
import com.onedrivex.service.XService;
import com.onedrivex.util.CommonUtil;
import com.onedrivex.util.Constants;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

@Controller
@ControllerAdvice
public class IndexController {
	
	private OneDriveApi api = OneDriveApi.getInstance();
	
	private AntPathMatcher urlMatcher = new AntPathMatcher();
	
	@Autowired
	private XService servive;
	
	@ModelAttribute("siteName")
	public String getSiteName() {
		return servive.getConfig("siteName");
	}
	
	/**
	 * 系统管理（基本配置）
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin")
	public String admin(Model model, @RequestParam(required = false) Map<String, String> config) {
		if(config.size() > 0) {
			servive.updateConfig(config);
			model.addAttribute("message", "修改成功");
		}
		model.addAttribute("config", Constants.globalConfig);
		return "classic/admin/settings";
	}
	
	/**
	 * 系统管理（设置密码）
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin/setpass")
	public String setpass(Model model, HttpServletRequest request, String old_pass, String password, String password2) {
		if(old_pass == null && password == null && password2 == null) {
		}else{
			String msg = servive.updatePass(old_pass, password, password2);
			model.addAttribute("message", msg);
			if(msg.equals("密码修改成功")) {
				HttpSession session = request.getSession();
				session.removeAttribute("isLogin");
			}
		}
		return "classic/admin/setpass";
	}
	
	/**
	 * 系统管理（缓存设置）
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin/cache")
	public String setcache(Model model, @RequestParam(required = false) Map<String, String> config) {
		if(config.size() > 0) {
			if(StrUtil.isBlank(config.get("openCache"))) {
				config.put("openCache", "1");
			}
			servive.updateConfig(config);
			//cron有修改
			CronUtil.remove(Constants.refreshCacheTaskId);
			Constants.refreshCacheTaskId = CronUtil.schedule(Constants.globalConfig.get("refreshCacheCron"), new Task() {
				@Override
				public void execute() {
					try {
						servive.refreshJob();
					} catch (Exception e) {
					}
				}
			});
			CronUtil.restart();
			Constants.timedCache.schedulePrune(Long.parseLong(Constants.globalConfig.get("cacheExpireTime"))*1000);
			model.addAttribute("message", "修改成功");
		}
		model.addAttribute("config", Constants.globalConfig);
		return "classic/admin/cache";
	}
	
	/**
	 * 手动清理缓存
	 * @return
	 */
	@RequestMapping("/admin/clearCache")
	@ResponseBody
	public String clearCache() {
		Constants.timedCache.clear();
		return "缓存清空成功";
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
	
	/**
	 * 密码登陆后台
	 * @param password
	 * @param request
	 * @return
	 */
	@RequestMapping("/login")
	public String login(String password, HttpServletRequest request, HttpServletResponse response, Model model) {
		 HttpSession session = request.getSession();
		 String pwd = Constants.globalConfig.get("password");
		 if(StrUtil.isNotBlank(password) && password.equals(pwd)) {
			session.setMaxInactiveInterval(24*60*60);
			session.setAttribute("isLogin", true);
			Cookie c=new Cookie("JSESSIONID", session.getId());
			c.setPath(request.getContextPath());
			c.setMaxAge(604800);
			response.addCookie(c);
			return "redirect:/admin";
		 }else if(StrUtil.isNotBlank(password) && !password.equals(pwd)) {
			 model.addAttribute("message", "密码不正确");
			 return "classic/admin/login";
		 }else {
			 return "classic/admin/login";
		 }
	}
	
	/**
	 * 退出登陆
	 * @param request
	 * @return
	 */
	@RequestMapping("/logout")
	public String logout(HttpServletRequest request) {
		HttpSession session = request.getSession();
		session.removeAttribute("isLogin");
		return "redirect:/admin";
	}
	
	/**
	 * 退出登陆
	 * @param request
	 * @return
	 */
	@RequestMapping("/file/upload")
	@ResponseBody
	public String upload(@RequestParam("file") MultipartFile file) {
		String path = Constants.globalConfig.get("uploadPath");
        if (!file.isEmpty()) {
            try {
            	if(!FileUtil.exist(path)) {
            		FileUtil.mkdir(path);
            	}
                BufferedOutputStream out = new BufferedOutputStream(
                        new FileOutputStream(new File(path+File.separator+file.getOriginalFilename())));
                out.write(file.getBytes());
                out.flush();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return "上传失败," + e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
                return "上传失败," + e.getMessage();
            }
            return "上传成功";
        } else {
            return "上传失败，因为文件是空的.";
        }
	}
	
	@RequestMapping("/**")
	public String index(Model model, HttpServletRequest request, HttpServletResponse response, Integer t, String password) {
		String parentPath = CommonUtil.getParentPath(request.getRequestURI());
		String path = URLUtil.decode(request.getRequestURI());
		if(path.equals("/") && !Constants.globalConfig.get("onedriveRoot").equals("/")) {
			return "redirect:"+Constants.globalConfig.get("onedriveRoot");
		}
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
					model.addAttribute("readme", servive.getReadme(ti, path+"/README.md"));
				}
				items = items.parallelStream().filter(r->!Constants.globalConfig.get("onedriveHide").contains(r.getPath())).collect(Collectors.toList());
				model.addAttribute("items", items);
			}else if(item != null && !item.getFolder()){
				String referer = request.getHeader("Referer");
				String host = request.getServerName();
				if(StrUtil.isBlank(referer)) {
					return "redirect:"+item.getDownloadUrl();
				}
				java.net.URL url = null;
	            try {
	                url = new java.net.URL(referer);
	            } catch (MalformedURLException e) {
	            }
	            if (host.equals(url.getHost())) {
	            	model.addAttribute("item", item);
	            	return CommonUtil.showORedirect(model, item, Constants.globalConfig.get("theme"), ti, t);
	            }else{
	            	if(checkWhiteList(Constants.globalConfig.get("onedriveHotlink"), url.getHost())){
	            		//下载
	            		return "redirect:"+item.getDownloadUrl();
	            	}else{
	            		return "redirect:/unauthorized";
	            	}
	            }
			}else {
				return Constants.globalConfig.get("theme")+"/404";
			}
		}
		return Constants.globalConfig.get("theme")+"/list";
	}
	
	/**
	 * 403未授权的访问
	 * @return
	 */
	@RequestMapping(value = "/unauthorized", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<HttpStatus> sendViaResponseEntity() {
	    return new ResponseEntity<HttpStatus>(HttpStatus.FORBIDDEN);
	}
	
	private Boolean checkWhiteList(String whiteList, String host) {
		if (StrUtil.isNotBlank(whiteList)) {
            for (String p : StrUtil.split(whiteList, ";")) {
            	if(urlMatcher.match(p, host)){
					//通过校验
					return true;
				}
            }
            
        }
        return false;
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
