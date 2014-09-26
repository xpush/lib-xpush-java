package io.stalk.xpush;

import io.stalk.xpush.exception.AuthorizationFailureException;
import io.stalk.xpush.model.Device;
import io.stalk.xpush.model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.emitter.Emitter.Listener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * This class is the main entry point for accessing XPush.
 *
 * <p>By creating a new {@link XPush} instance and calling {@link Pusher.login()} a connection to XPush is established.</p>
 *
 */
public class XPush extends Emitter{

	private static String SESSION = "SESSION";
	private static String CHANNEL = "CHANNEL";
	
	private static String ERROR_MESSAGE = "message";
	private static String RETURN_STATUS = "status";
	private static String RESULT 		= "result";
	private static String WARN 			= "WARN";
	private static String STATUS_OK 	= "ok";
	
	private static String ACTION_CREATE_CHANNEL 		= "channel-create";
	private static String ACTION_CHANNEL_LIST 			= "channel-list";
	private static String ACTION_ACTIVE_CHANNEL_LIST 	= "channel-list-active";
	public static String ACTION_GET_UNREADMESSAGES 		= "message-unread";
	public static String ACTION_RECEIVED_MESSAGE 		= "message-received";
	public static String ACTION_USER_LIST 				= "user-query";
	
	private int MAX_CONNECTION = 5;
	private long MAX_TIMEOUT = 30000;	
	
	public ApplicationInfo appInfo;
	public UserInfo mUser = new UserInfo();
	private ChannelConnection mSessionChannel;
	public ArrayList<JSONObject> _receiveMessageStack = new ArrayList<JSONObject>();
	private HashMap<String, ChannelConnection> mChannels = new HashMap<String, ChannelConnection>();
	
	public Boolean receivedReady = false;
	private Boolean isConnected = false;
	public Boolean isExistUnread = true;

	/**
	 * <p>
	 * Create a new instance of XPush.
	 * </p>
	 * 
	 * @param host Your XPush Session server address.
	 * @param appId Your application id in XPush.
	 */
	public XPush(String host, String appId){
		appInfo = new ApplicationInfo(host, appId);
	}
	
	/**
	 * <p>
	 * login to XPush and receive my own channel server address. Connect to static session server.
	 * </p>
	 * 
	 * @param userId Your user account id in XPush
	 * @param password password for account in XPush
	 * @param deviceId Your device 
	 * @return login result status
	 */
	public String login(String userId, String password, String deviceId){
		JSONObject sendData = new JSONObject();
		try {
			sendData.put( XPushData.APP_ID, this.appInfo.getAppId());
			sendData.put( XPushData.USER_ID, userId);
			sendData.put( XPushData.PASSWORD, password);
			sendData.put( XPushData.DEVICE_ID, deviceId);
			
			JSONObject resultO = asyncCall("auth", "POST", sendData);
			
			if("ok".equalsIgnoreCase( resultO.getString( RETURN_STATUS ) ) ){
				mUser.setUserId(userId);
				mUser.setDeviceId(deviceId);
				connectSessionSocket(resultO.getJSONObject(RESULT));
				return null;
			}else {
				return resultO.getString(ERROR_MESSAGE);
			}			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
		//{"status":"ok","result":{"token":"uBPdtRm4HJ","server":"249","serverUrl":"http://121.161.148.116:9992","user":{"A":"stalk-io","DS":{"WEB":{"N":null,"TK":"ZXumtvBoiS"}},"DT":null,"GR":[],"U":"notdol110","_id":"53c369784ee55a486f66b7a1"}}}
	}
	
	/**
	 * <p>
	 * signup to XPush.
	 * </p>
	 * 
	 * @param userId Your user account id in XPush
	 * @param password password for account in XPush
	 * @param deviceId Your device 
	 * @return 
	 */
	public void signup(String userId, String password, String deviceId) throws AuthorizationFailureException{
		JSONObject sendData = new JSONObject();
		JSONObject result = null;
		String error = null,status = null;
		try {
			sendData.put( XPushData.APP_ID, this.appInfo.getAppId());
			sendData.put( XPushData.USER_ID, userId);
			sendData.put( XPushData.PASSWORD, password);
			sendData.put( XPushData.DEVICE_ID, deviceId);
			
			result = asyncCall("user/register", "POST", sendData);
			
			status = result.getString(RETURN_STATUS);

			if(XPushData.ERROR_INTERNAL.equalsIgnoreCase(status)){
				error = result.getString(ERROR_MESSAGE);
				throw new AuthorizationFailureException(error);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void signup(String userId, String password, String deviceId, String notiId) throws AuthorizationFailureException{
		JSONObject sendData = new JSONObject();
		JSONObject result = null;
		String error = null,status = null;
		
		try {
			sendData.put( XPushData.APP_ID, this.appInfo.getAppId());
			sendData.put( XPushData.USER_ID, userId);
			sendData.put( XPushData.PASSWORD, password);
			sendData.put( XPushData.DEVICE_ID, deviceId);
			sendData.put( XPushData.NOTI_ID, notiId);
			result = asyncCall("user/register", "POST", sendData);
			
			status = result.getString(RETURN_STATUS);
			if(XPushData.ERROR_INTERNAL.equalsIgnoreCase(status)){
				error = result.getString(ERROR_MESSAGE);
				throw new AuthorizationFailureException(error);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void connectSessionSocket(JSONObject info){
		mSessionChannel = new ChannelConnection(this, ChannelConnection.SESSION, info);
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
		ChannelConnection tCh = null;
		if( mChannels.containsKey(chName) ){
			tCh = mChannels.get(chName);
		}else{
			tCh = createChannel(chName, ChannelConnection.CHANNEL);
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
			if(chName != null) sendValue.put(XPushData.CHANNEL_ID, chName);
			sendValue.put(XPushData.USER_ID, userList);			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//sendValue.addProperty(XPushData.CHANNEL_ID, chName);
		//sendValue.addProperty(XPushData.USER_ID, userList);
		final ChannelConnection ch = createChannel(chName, ChannelConnection.CHANNEL);
		
		this.sEmit(ACTION_CREATE_CHANNEL, sendValue, new Emitter.Listener() {
			
			public void call(Object... args) {
				// TODO Auto-generated method stub
				System.out.println("xpush : createChannel(receive)");
				
				if(args[0] != null && ChannelConnection.WARN_CHANNEL_EXIST.equalsIgnoreCase(args[0].toString())){
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
	
	public ChannelConnection createChannel(String chName, String mode){
		ChannelConnection rCh = new ChannelConnection(this,mode);
		
		if(chName != null){
			registChannel(chName, rCh);
		}
		
		return rCh;
	}
	
	public ChannelConnection getChannel(String chName){
		if(mChannels.containsKey(chName)){
			return mChannels.get(chName);
		}else{
			return null;
		}
	}
	
	private void registChannel(String chName, ChannelConnection ch){
		mChannels.put(chName, ch);
	}
	
	public void getChannels(final XPushEmitter.receiveChannelList cb){
		//[{"__v":0,"US":[{"D":"IM-A860K-C9166284","U":"notdol3000","N":""}],"_id":"stalk-io^W1_LwlKw7","A":"stalk-io","CD":"2014-09-16T15:35:09.475Z","C":"W1_LwlKw7"}]		
		this.sEmit(ACTION_CHANNEL_LIST, null, new Emitter.Listener() {
			
			public void call(Object... args) {
				// TODO Auto-generated method stub
				String err = (String)args[0];
				if(err == null){
					JSONArray channelList = (JSONArray)args[1];
					List<io.stalk.xpush.model.Channel> receivedChannels = new ArrayList<io.stalk.xpush.model.Channel>();
					try {
					for(int i = 0 ; i < channelList.length(); i++){
							JSONObject chO = channelList.getJSONObject(i);
							io.stalk.xpush.model.Channel receivedChannel = new io.stalk.xpush.model.Channel();
							
							receivedChannel.setChannelId( chO.getString(XPushData.CHANNEL_ID));
							
							SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
						    isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
						    Date date = isoFormat.parse(chO.getString(XPushData.CREATE_DATE) );
						    
							receivedChannel.setCreateDate(date);
							
							JSONArray users = chO.getJSONArray(XPushData.USER_IDS);
							ArrayList<User> channelUsers = new ArrayList<User>();
							for(int j=0 ; j < users.length(); j++){
								JSONObject user = users.getJSONObject(j);
								User uO = new User();
								uO.setId( user.getString(XPushData.USER_ID) );
								
								Device device = new Device();
								device.setDeviceId( user.getString(XPushData.DEVICE_ID));
								
								ArrayList<Device> devices = new ArrayList<Device>();
								devices.add(device);
								
								uO.setDevices(devices);
								
								channelUsers.add(uO);
							}
							receivedChannel.setUsers(channelUsers);
							
							receivedChannels.add(receivedChannel);
					}
					System.out.println("===== parse end");
					System.out.println(receivedChannels);
					cb.call(err, receivedChannels);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e){
						e.printStackTrace();
					}
					
					
				}else{
					cb.call(err,null);
				}
				
			}
		});
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
				if(status == null){
					JSONObject result = (JSONObject)args[1];
					List<User> receivedUsers = new ArrayList<User>();
					try {
					
					JSONArray friendList = result.getJSONArray("users");
					for(int i = 0 ; i < friendList.length(); i++){
						JSONObject userO = friendList.getJSONObject(i);
						User rUser = new User();
						rUser.setId(userO.getString(XPushData.USER_ID));
						System.out.print(userO.getString(XPushData.DATA));
						if(userO.getString(XPushData.DATA) != null || userO.getString(XPushData.DATA) != "null"){
							rUser.setData(userO.getJSONObject(XPushData.DATA));
						}
						receivedUsers.add(rUser);
					}
					
						cb.call(status, receivedUsers);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					cb.call(status);
				}
				
				
			}
		});
	}
	
	public String j(String key, String value){
		return "\""+key+"\" : \""+value+"\"";
	}
	
	public JSONObject asyncCall(String context, String method, JSONObject sendData ){
		
		URL url;
		int status = 0;
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
			System.out.println("======sendData "+sendData );
			final InputStream inputStream = urlConnection.getInputStream();
	        status = urlConnection.getResponseCode();
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
		System.out.println("==== status : "+status);
		return new JSONObject();
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
