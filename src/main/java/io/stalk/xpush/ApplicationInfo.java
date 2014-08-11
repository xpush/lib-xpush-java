package io.stalk.xpush;

public class ApplicationInfo {

	private String host = "127.0.0.1";
	private String appId;
	
	public ApplicationInfo( String appId){
		this.appId = appId;
	}	
	
	public ApplicationInfo(String host, String appId){
		this.host = host;
		this.appId = appId;
	}
	
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}

}
