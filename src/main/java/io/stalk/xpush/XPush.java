package io.stalk.xpush;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import com.github.nkzawa.emitter.Emitter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class XPush extends Emitter{

	private static String SESSION = "SESSION";
	private static String CHANNEL = "CHANNEL";
	
	private int MAX_CONNECTION = 5;
	private long MAX_TIMEOUT = 30000;	
	
	public ApplicationInfo appInfo;
	private HashMap<String, Channel> mChannels;
	private Channel mSessionChannel;
	
	public UserInfo mUser = new UserInfo();
	
	private Boolean isConnected = false;
	public Boolean isExistUnread = true;

	public XPush(String appId){
		appInfo = new ApplicationInfo(appId);
	}

	
	public XPush(String host, String appId){
		appInfo = new ApplicationInfo(host, appId);
	}
	
	public String login(String userId, String password, String deviceId){
		String result = asyncCall("/auth", "POST", "{" + j("A",this.appInfo.getAppId()) +" ,"+j("U", userId) +", "
				+ j("PW", password)+ ","+ j("D", deviceId)+ "}");
		JsonParser parser = new JsonParser();
		JsonObject resultO = (JsonObject)parser.parse(result);
		//{"status":"ok","result":{"token":"uBPdtRm4HJ","server":"249","serverUrl":"http://121.161.148.116:9992","user":{"A":"stalk-io","DS":{"WEB":{"N":null,"TK":"ZXumtvBoiS"}},"DT":null,"GR":[],"U":"notdol110","_id":"53c369784ee55a486f66b7a1"}}}
		if("ok".equalsIgnoreCase(resultO.get("status").getAsString()) ){
			mUser.setUserId(userId);
			mUser.setDeviceId(deviceId);
			connectSessionSocket(resultO.get("result").getAsJsonObject());
			return null;
		}else {
			return resultO.get("message").getAsString();
		}
	}
	
	public String signup(String userId, String password, String deviceId){
		String result = asyncCall("/user/register", "POST", "{" + j("A",this.appInfo.getAppId()) +" ,"+j("U", userId) +", "
				+ j("PW", password)+ ","+ j("D", deviceId)+ "}");
		
		return result;
	}
	
	public void connectSessionSocket(JsonObject info){
		mSessionChannel = new Channel(this, Channel.SESSION, info);
		mSessionChannel.connect(null);
	}
	
	private void createChannel(String[] users, String chName, Emitter.Listener cb ){
		
	} 
	
	public void send(String chName, String key, JsonObject value){
		Channel tCh = null;
		if( mChannels.containsKey(chName) ){
			tCh = mChannels.get(chName);
		}else{
			tCh = createChannel(chName);
		}
		
	}
	
	public Channel createChannel(String chName){
		Channel rCh = new Channel(this);
		
		
		
		return rCh;
	}
	
	
	public String j(String key, String value){
		return "\""+key+"\" : \""+value+"\"";
	}
	
	public String asyncCall(String context, String method, String sendData ){
		
		URL url;
		try {
			url = new URL(this.appInfo.getHost() + "/" + context);
			final HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
	        urlConnection.setRequestMethod(method);
			urlConnection.setDoOutput(true);
			urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			urlConnection.connect();
			final OutputStream outputStream = urlConnection.getOutputStream();
			outputStream.write((sendData).getBytes("UTF-8"));
			outputStream.flush();
			
	        int status = urlConnection.getResponseCode();
			
			final InputStream inputStream = urlConnection.getInputStream();
	        switch (status) {
	            case 200:
	            case 201:
	                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
	                StringBuilder sb = new StringBuilder();
	                String line;
	                while ((line = br.readLine()) != null) {
	                    sb.append(line+"\n");
	                }
	                br.close();
	                return sb.toString();
	        }

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
}
