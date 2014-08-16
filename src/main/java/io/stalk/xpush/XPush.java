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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nkzawa.emitter.Emitter;
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

	
	private static String STATUS_OK = "ok";
	
	private static String APP_ID = "A";
	private static String KEY_CHANNEL = "C";
	private static String KEY_USER = "U";
	
	private int MAX_CONNECTION = 5;
	private long MAX_TIMEOUT = 30000;	
	
	public ApplicationInfo appInfo;
	private HashMap<String, Channel> mChannels = new HashMap<String, Channel>();
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
		System.out.println("===== XPush : login");
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
		
		tCh.send(key, value, cb);
		
	}
	
	public void createChannel(String[] users, final String chName, JsonObject datas, final Emitter.Listener cb){
		
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
			sendValue.put(KEY_CHANNEL, chName);
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
				System.out.println("====== XPush : createChannel");
				
				if(args[0] != null && !Channel.WARN_CHANNEL_EXIST.equalsIgnoreCase(args[0].toString())){
					System.out.println("======= create channel error : " + args[0]);
					return;
				}
				
				JSONObject result = new JSONObject(args[1]);
//{"US":[{"D":"WEB","U":"notdol101","N":null},
//{"D":"WEB","U":"notdol102","N":null}],
//"_id":"stalk-io^tempChannel","A":"stalk-io","CD":"2014-08-16T06:19:14.136Z","C":"tempChannel","__v":0}
				String realChName = chName;
				if(chName == null){
					try {
						registChannel( result.getString( XPushData.CHANNEL_ID ) , ch);
						realChName = chName;
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				try {
					JSONObject result2 = getChannelInfo(realChName);
					System.out.println("====== result: "+result2);
					ch.setServerInfo(result2.getJSONObject(RESULT));
					ch.connect(null);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				cb.call();
					
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
			System.out.println(this.appInfo.getHost() + "/" + context);
	        if(!method.equalsIgnoreCase("GET")){
			final OutputStream outputStream = urlConnection.getOutputStream();
			outputStream.write(/*(sendData).getBytes("UTF-8")*/ sendData.toString().getBytes("UTF-8"));
			outputStream.flush();
	        }
			System.out.println("======sendData "+sendData );
			final InputStream inputStream = urlConnection.getInputStream();
	        int status = urlConnection.getResponseCode();
			System.out.println("====== "+status);
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

		} catch (MalformedURLException e) {
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
	
	
	
	private void sEmit(final String key, JSONObject value, final Emitter.Listener cb){
		
		this.mSessionChannel.send(key, value, new Emitter.Listener() {
			
			public void call(Object... args) {
				// TODO Auto-generated method stub
				//String status = ((JsonObject)args[0]).getAsJsonPrimitive( RETURN_STATUS ).getAsString();
				//String message = ((JsonObject)args[0]).getAsJsonPrimitive( ERROR_MESSAGE ).getAsString();

				String status;
				String message;
				System.out.println(args);
				try {
					status = ((JSONObject)args[0]).getString( RETURN_STATUS );
					System.out.println("key : "+key+ " -- "+"status : "+status+" -- ");
					if( STATUS_OK.equalsIgnoreCase(status)){
						//cb.call(null, ((JsonObject)args[0]).getAsJsonObject( RETURN ) );
						cb.call(null, ((JSONObject)args[0]).get( RESULT ));
					}else{
						message = ((JSONObject)args[0]).getString( ERROR_MESSAGE );
						
						if(status.indexOf( WARN ) == 0){
							System.out.println("=== xpush warn : "+key +":"+status+":"+message);
						}else{
							System.out.println("=== xpush error : "+key +":"+status+":"+message);
						}
						cb.call(status,message);
					}
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	
}
