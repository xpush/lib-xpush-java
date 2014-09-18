package io.stalk.xpush.model;

import java.util.Date;

import org.json.JSONObject;

public class Message {
	private String channelId;
	private String key;
	private JSONObject value;
	private Date timestamp;
	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public JSONObject getValue() {
		return value;
	}
	public void setValue(JSONObject value) {
		this.value = value;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}
