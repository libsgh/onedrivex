package com.onedrivex.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;

import com.onedrivex.api.CodeType;
import com.onedrivex.api.Item;
import com.onedrivex.api.TokenInfo;

import cn.hutool.core.text.StrSpliter;
import cn.hutool.core.util.EscapeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;

public class CommonUtil {
	
	public static String getFormatSize(double size) {  
        double kiloByte = size/1024;
        if(kiloByte == 0.0) {
        	return 0 + "B";
        }
        if(kiloByte < 1) {  
            return size + "B";  
        }  
          
        double megaByte = kiloByte/1024;  
        if(megaByte < 1) {  
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));  
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";  
        }  
          
        double gigaByte = megaByte/1024;  
        if(gigaByte < 1) {  
            BigDecimal result2  = new BigDecimal(Double.toString(megaByte));  
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";  
        }  
          
        double teraBytes = gigaByte/1024;  
        if(teraBytes < 1) {  
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));  
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";  
        }  
        BigDecimal result4 = new BigDecimal(teraBytes);  
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";  
    }
	
	/**
	 * 根据文件类型返回ico后缀
	 * @param name
	 * @return
	 */
	public static String fileIco(String name) {
		String ext = StrUtil.subAfter(name, ".", true).toLowerCase();
		if(StrUtil.equalsAny(ext, new String[] {"7z","txt","ai","avi","eps","exe","flv",
				"gif","mov","html","mp4","pdf","mp3","png","psd","rar","svg","swf","rp",
				"tif","jpg","tar","xsl","zip","js","json","csv"})) {
			return "<i class=\"mdui-icon iconfont icon-file_"+ext+"\"></i>";
		}
		if(StrUtil.equalsAny(ext, new String[] {"bmp","jpg","jpeg","png","gif"})) {
			return "<i class=\"mdui-icon iconfont icon-file_image\"></i>";
			//return "<i class=\"mdui-icon material-icons mdui-text-color-theme-icon\">image</i>";
		}else if(StrUtil.equalsAny(ext, new String[] {"mp4","mkv","webm","avi","mpg", "mpeg", "rm", "rmvb", "mov", "wmv", "mkv", "asf"})) {
			return "<i class=\"mdui-icon iconfont icon-file_video\"></i>";
			//return "<i class=\"mdui-icon material-icons mdui-text-color-theme-icon\">ondemand_video</i>";
		}else if(StrUtil.equalsAny(ext, new String[] {"ogg","mp3","wav"})) {
			return "<i class=\"mdui-icon iconfont icon-file_audio\"></i>";
			//return "<i class=\"mdui-icon material-icons mdui-text-color-theme-icon\">audiotrack</i>";
		}else if(StrUtil.equalsAny(ext, new String[] {"pdf"})) {
			return "<i class=\"mdui-icon material-icons mdui-text-color-theme-icon\">picture_as_pdf</i>";
		}else if(StrUtil.containsAny(ext, "html","htm","php","css","go","java","js","json","txt","sh","md")) {
			return "<i class=\"mdui-icon iconfont icon-file_code\"></i>";
		}else if(StrUtil.containsAny(ext, "doc","docx")) {
			return "<i class=\"mdui-icon iconfont icon-file_doc\"></i>";
		}else if(StrUtil.containsAny(ext, "ppt", "pptx")) {
			return "<i class=\"mdui-icon iconfont icon-file_ppt\"></i>";
		}else if(StrUtil.containsAny(ext, "xls", "xlsx")) {
			return "<i class=\"mdui-icon iconfont icon-file_excel\"></i>";
		}else{
			return "<i class=\"mdui-icon iconfont icon-file_multiple\"></i>";
		}	
	}
	/**
	 * 获取文件类型（后缀）
	 * @param name
	 * @return
	 */
	public static String fileType(String name) {
		return StrUtil.subAfter(name, ".", true).toLowerCase();
	}
	
	public static String getParentPath(String requestURI) {
		if(requestURI.equals("/")) {
			return "";
		}else {
			List<String> list = StrSpliter.splitPath(requestURI);
			if(list.size() == 1) {
				return "/";
			}else {
				list.remove(list.size()-1);
				return "/"+StrUtil.join("/", list);
			}
		}
	}
	
	private static void getAllPaths(String currentPath, List<Map<String, String>> list) {
		String pPath = getParentPath(currentPath);
		Map<String, String> map = new HashMap<String, String>();
		map.put(getNameByPath(pPath), pPath);
		list.add(map);
		if(!pPath.equals("/")) {
			getAllPaths(pPath, list);
		}
	}
	
	public static List<Map<String, String>> getAllPaths(String currentPath) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Map<String, String> map = new HashMap<String, String>();
		map.put(getNameByPath(currentPath), currentPath);
		list.add(map);
		if(!currentPath.equals("/")) {
			getAllPaths(currentPath, list);
		}
		Collections.reverse(list);
		return list;
	}
	
	public static String getNameByPath(String path){
		String[] arr = path.split("/");
		if(arr.length > 0) {
			return URLUtil.decode(arr[arr.length-1]);
		}else{
			return URLUtil.decode(path);
		}
	}

	public static String showORedirect(Model model, Item item, String theme, TokenInfo ti, Integer t, HttpServletRequest request) {
		if(t != null) {
			return "redirect:"+ StrUtil.subBefore(item.getThumb(), "&width=", true)+"&width="+t+"&height="+t;
		}
		if(StrUtil.containsAny(item.getExt(), Constants.globalConfig.get("showVideo").split(" "))) {
			return theme+"/show/video";
		}else if(StrUtil.containsAny(item.getExt(), Constants.globalConfig.get("showAudio").split(" "))) {
			return theme+"/show/audio";
		}else if(StrUtil.containsAny(item.getExt(), Constants.globalConfig.get("showImage").split(" "))) {
			return theme+"/show/image";
		}else if(StrUtil.containsAny(item.getExt(), Constants.globalConfig.get("showCode").split(" "))) {
			String content = HttpUtil.downloadString(item.getDownloadUrl(), "UTF-8");
			model.addAttribute("codeType",CodeType.get(item.getExt()));
			model.addAttribute("content", EscapeUtil.escapeHtml4(content));
			return theme+"/show/code";
		}else if(StrUtil.containsAny(item.getExt(), Constants.globalConfig.get("showPdf").split(" "))) {
			return theme+"/show/pdf";
		}else if(StrUtil.containsAny(item.getExt(), Constants.globalConfig.get("showDoc").split(" "))) {
			String onlineViewUrl = "https://view.officeapps.live.com/op/view.aspx?src=" + URLUtil.encode(request.getRequestURL().toString());
			return "redirect:" + onlineViewUrl;
		}
		return "redirect:" + item.getDownloadUrl();
	}
	
	public static String getCookie(HttpServletRequest request, String cookieName){

	        Cookie[] cookies =  request.getCookies();
	        if(cookies != null){
	            for(Cookie cookie : cookies){
	                if(cookie.getName().equals(cookieName)){
	                    return cookie.getValue();
	                }
	            }
	        }
	        return null;
	 }
	
	public static byte[] File2byte(File file){
        byte[] buffer = null;
        try{
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1){
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return buffer;
    }
	
}
