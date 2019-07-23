package com.onedrivex.api;

import cn.hutool.core.util.NumberUtil;

public class Task {
	
	private String itemId;//上传成功的itemId
	
	private String remotePath;
	
	private String uploadUrl;
	
	private long fileSize;//文件大小
	
	private String humanFileSize;//文件大小(人类可读)
	
	private int status;//任务状态0：等待上传，1，上传中，2上传成功，3上传失败，4取消上传
	
	private long uploadSize;//上传大小
	
	private String percent;//百分比
	
	private String speed;//速度

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getRemotePath() {
		return remotePath;
	}

	public void setRemotePath(String remotePath) {
		this.remotePath = remotePath;
	}

	public String getUploadUrl() {
		return uploadUrl;
	}

	public void setUploadUrl(String uploadUrl) {
		this.uploadUrl = uploadUrl;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getHumanFileSize() {
		return humanFileSize;
	}

	public void setHumanFileSize(String humanFileSize) {
		this.humanFileSize = humanFileSize;
	}

	public long getUploadSize() {
		return uploadSize;
	}

	public void setUploadSize(long uploadSize) {
		this.uploadSize = uploadSize;
	}

	public String getPercent() {
		double c = NumberUtil.div(uploadSize, fileSize, 2);
		return NumberUtil.decimalFormat("#.##%", c);
	}

	public void setPercent(String percent) {
		this.percent = percent;
	}

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
}
