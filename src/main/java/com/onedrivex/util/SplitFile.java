package com.onedrivex.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.onedrivex.api.UploadInfo;

import cn.hutool.core.io.FileUtil;

public class SplitFile {
	//文件
	private File file;
	//文件名
	private String filename;
	//文件大小
	private long length;
	//块数
	private int size;
	//每块大小
	private long blocksize;
	//每块名称
	private List<String> blockpath;
	
	public SplitFile() {
	    blockpath=new ArrayList<String>();

	}   
	public SplitFile(File file) {

	   this(file,1024);

	}   
	public SplitFile(File file,long blocksize) {
	    this();
	    this.file=file;
	    this.blocksize=blocksize;                       
	}
	/**
	 *初始化 计算块数，确定文件名
	 *
	 */
	public  void init() {
	    File src=null;
	    //确保健壮性
	    if(file==null||!((src=file).exists())) {
	        return;
	    }
	    if(src.isDirectory()) {//文件夹不能分
	        return;
	    }                   
	    this.filename=src.getName();
	    //计算块数实际大小与每块大小
	    this.length=src.length();
	    //修正大小
	    if(length<this.blocksize) {
	        this.blocksize=length;
	    }
	    size=(int)Math.ceil(length*1.0/this.blocksize);
	}

	public void inpathname(String destpath) {
	    for(int i=0;i<size;i++) {
	        this.blockpath.add(destpath+"/"+filename+".part"+i);
	    }
	}
	
	/**
	 * 分割
	 * @param index
	 * @param beginpoint
	 * @param actrualblocksize
	 * @throws IOException 
	 */
	public UploadInfo spiltdetail(int index, long off,long len) {
	    File file1=new File(blockpath.get(index));//目标
	    FileUtil.writeBytes(FileUtil.readBytes(file), file1, (int)off, (int)len, false);
	    UploadInfo info = new UploadInfo(off, off+len-1, FileUtil.readBytes(file1), file1.length());
	    System.out.println(info.toString());
	    return info;
	}
	/**
	 * 文件分割
	 * @param destpath
	 * @throws IOException 
	 */
	public List<UploadInfo> spiltfile(String destpath){
	    //确定文件路径
	    inpathname(destpath);
	    long off=0;//起始点
	    long len=blocksize;//每块大小
	    List<UploadInfo> list = new ArrayList<UploadInfo>();
	    for(int i=0;i<size;i++) {
	        if(i==size-1) {
	        	len=this.length-off;
	        }           
	        list.add(spiltdetail(i, off, len));
	        off+=len;//终点又是起点
	    }
	    return list;

	}
	
}
