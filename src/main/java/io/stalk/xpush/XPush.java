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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.emitter.Emitter.Listener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class XPush extends Emitter{

	private static String SESSION = "SESSION";
	private static String CHANNEL = "CHANNEL";
	
	private static String ERROR_MESSAGE = "message";
	private static String RETURN_STATUS = "status";
	private static String RESULT = "result";
	private static String WARN = "WARN";
	
	private static String ACTION_CREATE_CHANNEL = "channel-create";
	private static String ACTION_CHANNEL_LIST = "channel-list";
	private static String ACTION_ACTIVE_CHANNEL_LIST = "channel-list-active";
	public static String ACTION_GET_UNREADMESSAGES = "message-unread";
	public static String ACTION_RECEIVED_MESSAGE = "message-received";
	
	public static String ACTION_USER_LIST = "user-query";
	
	private static String STATUS_OK = "ok";
	
	private static String APP_ID = "A";
	private static String KEY_CHANNEL = "C";
	private static String KEY_USER = "U";
	
	private int MAX_CONNECTION = 5;
	private long MAX_TIMEOUT = 30000;	
	
	public ApplicationInfo appInfo;
	private HashMap<String, Channel> mChannels = new HashMap<String, Channel>();
	private Channel mSessionChannel;
	
	public ArrayList<JSONObject> _receiveMessageStack = new ArrayList<JSONObject>();
	public Boolean receivedReady = false;
	
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
		System.out.println("xpush: 0.login");
		JSONObject sendData = new JSONObject();
		try {
			sendData.put( XPushData.APP_ID, this.appInfo.getAppId());
			sendData.put( XPushData.USER_ID, userId);
			sendData.put( XPushData.PASSWORD, password);
			sendData.put( XPushData.DEVICE_ID, deviceId);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONObject resultO = asyncCall("auth", "POST", sendData);
		//JsonParser parser = new JsonParser();
		//JsonObject resultO = (JsonObject)parser.parse(result);
		try {
			if("ok".equalsIgnoreCase( resultO.getString( RETURN_STATUS ) ) ){
				mUser.setUserId(userId);
				mUser.setDeviceId(deviceId);
				connectSessionSocket(resultO.getJSONObject(RESULT));
				return null;
			}else {
				return resultO.getString(ERROR_MESSAGE);
			}			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		//{"status":"ok","result":{"token":"uBPdtRm4HJ","server":"249","serverUrl":"http://121.161.148.116:9992","user":{"A":"stalk-io","DS":{"WEB":{"N":null,"TK":"ZXumtvBoiS"}},"DT":null,"GR":[],"U":"notdol110","_id":"53c369784ee55a486f66b7a1"}}}
	}
	
	public JSONObject signup(String userId, String password, String deviceId){
		JSONObject sendData = new JSONObject();
		JSONObject result = null;
		try {
			sendData.put( XPushData.APP_ID, this.appInfo.getAppId());
			sendData.put( XPushData.USER_ID, userId);
			sendData.put( XPushData.PASSWORD, password);
			sendData.put( XPushData.DEVICE_ID, deviceId);
			
			result = asyncCall("user/register", "POST", sendData);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public JSONObject signup(String userId, String password, String deviceId, String notiId){
		JSONObject sendData = new JSONObject();
		JSONObject result = null;
		try {
			sendData.put( XPushData.APP_ID, this.appInfo.getAppId());
			sendData.put( XPushData.USER_ID, userId);
			sendData.put( XPushData.PASSWORD, password);
			sendData.put( XPushData.DEVICE_ID, deviceId);
			sendData.put( XPushData.NOTI_ID, notiId);
			result = asyncCall("user/register", "POST", sendData);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	
	
	public void connectSessionSocket(JSONObject info){
		mSessionChannel = new Channel(this, Channel.SESSION, info);
		try {
			mSessionChannel.connect(null);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void createChannel(String[] users, String chName, Emitter.Listener cb ){
		
	} 
	
	public void send(String chName, String key, JSONObject value, Emitter.Listener cb){
		Channel tCh = null;
		if( mChannels.containsKey(chName) ){
			tCh = mChannels.get(chName);
		}else{
			tCh = createChannel(chName, Channel.CHANNEL);
		}
		
		tCh.sendMessage(key, value, cb);
	}
	
	public void createChannel(String[] users, final String chName, JSONObject datas, final Emitter.Listener cb){
		System.out.println("xpush: createChannel");
		// add my id;
		boolean isExistSelf = false;
		JSONArray userList = new JSONArray();
		for( int i = 0 ; i < users.length;i++){
			if(users[i] == mUser.getUserId()){
				isExistSelf = true;
			}
			userList.put(users[i]);
		}
		
		//if(isExistSelf  == false ){ users[users.length] = mUser.getUserId();};
		if(isExistSelf == false) { userList.put( mUser.getUserId() ); };
		
		JSONObject sendValue = new JSONObject();
		//JsonObject sendValue = new JsonObject();
		try {
			if(chName != null) sendValue.put(KEY_CHANNEL, chName);
			sendValue.put(KEY_USER, userList);			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//sendValue.addProperty(KEY_CHANNEL, chName);
		//sendValue.addProperty(KEY_USER, userList);
		final Channel ch = createChannel(chName, Channel.CHANNEL);
		
		this.sEmit(ACTION_CREATE_CHANNEL, sendValue, new Emitter.Listener() {
			
			public void call(Object... args) {
				// TODO Auto-generated method stub
				System.out.println("xpush : createChannel(receive)");
				
				if(args[0] != null && Channel.WARN_CHANNEL_EXIST.equalsIgnoreCase(args[0].toString())){
					System.out.println("xxxxx xpush: createChannel " + args[0]);
					return;
				}
				
				JSONObject result = (JSONObject)args[1];//new JSONObject(args[1]);
				
//{"US":[{"D":"WEB","U":"notdol101","N":null},
//{"D":"WEB","U":"notdol102","N":null}],
//"_id":"stalk-io^tempChannel","A":"stalk-io","CD":"2014-08-16T06:19:14.136Z","C":"tempChannel","__v":0}
				String realChName = chName;
				if(chName == null){
					try {
						realChName = result.getString( XPushData.CHANNEL_ID );
						registChannel( realChName , ch);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				try {
					JSONObject result2 = getChannelInfo(realChName);
					ch.setServerInfo(result2.getJSONObject(RESULT));
					ch.connect(null);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				cb.call(null, realChName,ch);
					
			}
		});
		
	}
	
	public Channel createChannel(String chName, String mode){
		Channel rCh = new Channel(this,mode);
		
		if(chName != null){
			registChannel(chName, rCh);
		}
		
		return rCh;
	}
	
	public Channel getChannel(String chName){
		if(mChannels.containsKey(chName)){
			return mChannels.get(chName);
		}else{
			return null;
		}
	}
	
	private void registChannel(String chName, Channel ch){
		mChannels.put(chName, ch);
	}
	
	public void getChannels(Emitter.Listener cb){
		this.sEmit(ACTION_CHANNEL_LIST, new JSONObject(), cb);
	}
	
	public void getChannelsActive(JSONObject data, Emitter.Listener cb){
		// data.key (options)
		this.sEmit(ACTION_ACTIVE_CHANNEL_LIST, new JSONObject(), cb);
		// app, channel, created
	}
	
	public JSONObject getChannelInfo(String chNm){
//{"result":{"seq":"WJ5hNWpaZ","server":{"name":"23","channel":"tempChannel","url":"http://192.168.0.6:9991"},"channel":"tempChannel"},"status":"ok"}	
		return asyncCall( "node/"+ this.appInfo.getAppId() + '/' + chNm, "GET", new JSONObject());
	}
	
	public void getUserList(JSONObject params, final Emitter.Listener cb){

		if(params == null ) params = new JSONObject();
		try {
			params.put("query", new JSONObject());
			params.put("column", new JSONObject().put("U",1).put("DT",1).put("_id",0));
			params.put("options", new JSONObject().put("skipCount",true).put("sortBy",new JSONObject().put("DT.NM", 1)));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    /*  
		var params = {
	    	        query : _q,
	    	        column: { U: 1, DT: 1, _id: 0 },
	    	        options: {
	    	          skipCount : true,
	    	          sortBy : { 'DT.NM': 1}
	    	        }
	    	      };
		*/
		this.sEmit(ACTION_USER_LIST, params == null ? new JSONObject() : params , new Emitter.Listener() {
			
			public void call(Object... args) {
				// TODO Auto-generated method stub
				String status = (String)args[0];
				JSONObject result = (JSONObject)args[1];
				try {
					cb.call(result.getJSONArray("users"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
	}
	
	public String j(String key, String value){
		return "\""+key+"\" : \""+value+"\"";
	}
	
	public JSONObject asyncCall(String context, String method, JSONObject sendData ){
		
		URL url;
		try {
			url = new URL(this.appInfo.getHost() + "/" + context);
			final HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
	        if(!method.equalsIgnoreCase("GET")){
		        urlConnection.setRequestMethod(method);	        	
	        	urlConnection.setDoOutput(true);
	        }

			urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			urlConnection.connect();
			//System.out.println(this.appInfo.getHost() + "/" + context);
	        if(!method.equalsIgnoreCase("GET")){
			final OutputStream outputStream = urlConnection.getOutputStream();
			outputStream.write(/*(sendData).getBytes("UTF-8")*/ sendData.toString().getBytes("UTF-8"));
			outputStream.flush();
	        }
			//System.out.println("======sendData "+sendData );
			final InputStream inputStream = urlConnection.getInputStream();
	        int status = urlConnection.getResponseCode();
			//System.out.println("====== "+status);
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
	                String result = sb.toString();
	                JSONObject resultObj = new JSONObject(result);
	                return resultObj;
	        }

		}
		catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	

	public void sEmit(final String key, JSONObject value, final Emitter.Listener cb){
		
		this.mSessionChannel.send(key, value, new Emitter.Listener() {
			
			public void call(Object... args) {
				// TODO Auto-generated method stub
				//String status = ((JsonObject)args[0]).getAsJsonPrimitive( RETURN_STATUS ).getAsString();
				//String message = ((JsonObject)args[0]).getAsJsonPrimitive( ERROR_MESSAGE ).getAsString();

				String status;
				String message;
				if(key.equalsIgnoreCase("message-unread")){
					//System.out.println("======= what the ");
				}
				try {
					JSONObject result = null;
					/*
					if(args[0] instanceof String){
						result = new JSONObject(args[0]);
					}else{
						result = (JSONObject)args[0];
					}
					*/
					result = (JSONObject)args[0];
					
					status = (result).getString( RETURN_STATUS );
					//System.out.println("key : "+key+ " -- "+"status : "+status+" -- ");
					if( STATUS_OK.equalsIgnoreCase(status)){
						//cb.call(null, ((JsonObject)args[0]).getAsJsonObject( RETURN ) );
						if(cb!=null) cb.call(null, (result).get( RESULT ));
					}else{
						message = (result).getString( ERROR_MESSAGE );
						
						if(status.indexOf( WARN ) == 0){
							System.out.println("xxxxx xpush warn : "+key +":"+status+":"+message);
						}else{
							System.out.println("xxxxx xpush error : "+key +":"+status+":"+message);
						}
						if(cb != null) cb.call(status,message);
					}
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	public void receivedMessageFlush(){
		try {
			while(_receiveMessageStack.size() > 0 ){
				JSONObject message = _receiveMessageStack.remove(0);
					this.emit(message.getString("EVENT"), message.get("ARGS"));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		receivedReady = true;
	}
	
    public Emitter emit(String event, Object... args) {
    	if(receivedReady){
    		super.emit(event, args);
    	}else{
    		JSONObject newEvent = new JSONObject();
    		try {
				newEvent.put("EVENT", event);
	    		newEvent.put("ARGS", args);				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		_receiveMessageStack.add(newEvent);
    	}
        return this;
    }
	
	
}
