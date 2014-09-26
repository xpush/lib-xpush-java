package io.stalk.xpush.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class User {
	private String id;
	private String appId;
	private List<String> group;
	private List<Device> devices;
	
	private JSONObject data;
	
	public User(){
		
	}
	
	public User(String id){
		this.id = id;
	}

	public User(String id, Device device){
		this.id = id;
		this.devices = new ArrayList<Device>();
		devices.add(device);
	}
	
	public User(String id, List<Device> devices){
		this.id = id;
		this.devices = devices;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public List<String> getGroup() {
		return group;
	}
	public void setGroup(List<String> group) {
		this.group = group;
	}
	public List<Device> getDevices() {
		return devices;
	}
	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}
	public JSONObject getData() {
		return data;
	}
	public void setData(JSONObject data) {
		this.data = data;
	}
	
	public String toString(){
		return "{" + "'U' : '"+id+"'" + "'DS' : '"+devices+"'" +"}";
	}
}
