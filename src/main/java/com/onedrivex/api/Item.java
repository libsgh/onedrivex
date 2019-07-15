package com.onedrivex.api;

/**
 * 文件夹或是文件条目
 * @author Libs
 * 
 */
public class Item {
	
	private String id;//itemId
	
	private String name;//文件或文件夹名称
	
	private String size;//人类可读的文件或文件夹大小
	
	private String lastModifiedDateTime;//上一次修改时间
	
	private Boolean folder;//是不是文件夹，false or true
	
	private Integer childCount;//当为文件夹时子节点的数量，文件时为0
	
	private String downloadUrl;//下载地址
	
	private String ext;//文件类型（后缀）
	
	private String icon;//文件列表图标标识
	
	private String path;//路径
	
	private String thumb;//略缩图
	
	private String fileType;//文件类型


	public Item() {
		super();
	}

	public Item(String id, String name, String size, String lastModifiedDateTime, Boolean folder, Integer childCount, String downloadUrl, String ext, String icon, String path, String thumb, String fileType) {
		super();
		this.id = id;
		this.name = name;
		this.size = size;
		this.lastModifiedDateTime = lastModifiedDateTime;
		this.folder = folder;
		this.childCount = childCount;
		this.downloadUrl = downloadUrl;
		this.ext = ext;
		this.icon = icon;
		this.path = path;
		this.thumb = thumb;
		this.fileType = fileType;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getLastModifiedDateTime() {
		return lastModifiedDateTime;
	}

	public void setLastModifiedDateTime(String lastModifiedDateTime) {
		this.lastModifiedDateTime = lastModifiedDateTime;
	}

	public Boolean getFolder() {
		return folder;
	}

	public void setFolder(Boolean folder) {
		this.folder = folder;
	}

	public Integer getChildCount() {
		return childCount;
	}

	public void setChildCount(Integer childCount) {
		this.childCount = childCount;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getThumb() {
		return thumb;
	}

	public void setThumb(String thumb) {
		this.thumb = thumb;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Override
	public String toString() {
		return "Item [id=" + id + ", name=" + name + ", size=" + size + ", lastModifiedDateTime=" + lastModifiedDateTime
				+ ", folder=" + folder + ", childCount=" + childCount + ", downloadUrl=" + downloadUrl + ", ext=" + ext
				+ ", icon=" + icon + ", path=" + path + ", thumb=" + thumb + ", fileType=" + fileType + "]";
	}

}
