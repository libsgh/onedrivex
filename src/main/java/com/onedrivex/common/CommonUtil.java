package com.onedrivex.common;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.core.text.StrSpliter;
import cn.hutool.core.util.StrUtil;

public class CommonUtil {
	
	public static String getFormatSize(double size) {  
        double kiloByte = size/1024;  
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
		String ext = StrSpliter.split(name, ".", true, true).get(1).toLowerCase();
		if(StrUtil.equalsAny(ext, new String[] {"bmp","jpg","jpeg","png","gif"})) {
			return "image";
		}else if(StrUtil.equalsAny(ext, new String[] {"mp4","mkv","webm","avi","mpg", "mpeg", "rm", "rmvb", "mov", "wmv", "mkv", "asf"})) {
			return "ondemand_video";
		}else if(StrUtil.equalsAny(ext, new String[] {"ogg","mp3","wav"})) {
			return "audiotrack";
		}else{
			return "insert_drive_file";
		}	
	}
	
	public static String getParentPath(String requestURI) {
		if(requestURI.equals("/")) {
		}else {
			List<String> list = StrSpliter.splitPath(requestURI);
			if(list.size() == 1) {
				return "/";
			}else {
				list.remove(list.size()-1);
				return "/"+StrUtil.join("/", list);
			}
		}
		return requestURI;
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
		getAllPaths(currentPath, list);
		Collections.reverse(list);
		return list;
	}
	
	public static String getNameByPath(String path){
		String[] arr = path.split("/");
		if(arr.length > 0) {
			return arr[arr.length-1];
		}else{
			return path;
		}
	}
	public static void main(String[] args) {
		System.out.println(getAllPaths("/doc/bigdata").toString());
	}
}
