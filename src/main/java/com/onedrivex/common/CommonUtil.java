package com.onedrivex.common;

import java.math.BigDecimal;

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
	
}
