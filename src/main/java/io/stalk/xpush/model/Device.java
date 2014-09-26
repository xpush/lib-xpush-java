package io.stalk.xpush.model;

public class Device {
	private String deviceId;
	private String notificationId;
	
	public Device(){
		
	}
	
	public Device(String deviceId){
		this.deviceId = deviceId;
	}
	
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getNotificationId() {
		return notificationId;
	}
	public void setNotificationId(String notificationId) {
		this.notificationId = notificationId;
	}
	
	public String toString(){
		return "{" + "'D' : '"+deviceId+"'" +"}";
	}
}
