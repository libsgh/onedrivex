package com.onedrivex.api;

import java.io.File;

public class UploadInfo {
	
	private long begin;//起始
	
	private long end;//截止
	
	private byte[] bytes;//文件bytes
	
	private long length;//内容长度
	
	private File file;

	public UploadInfo() {
		super();
	}

	public UploadInfo(long begin, long end, byte[] bytes, long length, File file) {
		super();
		this.begin = begin;
		this.end = end;
		this.bytes = bytes;
		this.length = length;
		this.file = file;
	}

	public long getBegin() {
		return begin;
	}

	public void setBegin(long begin) {
		this.begin = begin;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	@Override
	public String toString() {
		return "UploadInfo [begin=" + begin + ", end=" + end + ", length="
				+ length + ", path="+file.getAbsolutePath()+"]";
	}

}
