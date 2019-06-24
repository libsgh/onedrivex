package com.onedrivex.api;

public enum CodeType {
	html("html","html"),
	htm("htm","html"),
	php("php","php"),
	css("css","css"),
	go("go","golang"),
	java("java","java"),
	js("js","javascript"),
	json("json","json"),
	txt("txt","Text"),
	sh("sh","sh"),
	md("md","Markdown");
	
	private String ext;
	
	private String ct;
	
	private CodeType(String ext, String ct) {
		this.ext = ext;
		this.ct = ct;
	}

	public String getExt() {
		return ext;
	}

	public String getCt() {
		return ct;
	}
	public static String get(String ext) {
		for (CodeType codeType : CodeType.values()) {
			if (codeType.getExt().equals(ext)) {
				return codeType.getCt();
			}
		}
		return null;
	}
}
