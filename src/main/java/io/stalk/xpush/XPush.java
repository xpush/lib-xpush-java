package io.stalk.xpush;

import io.stalk.xpush.exception.AuthorizationFailureException;
import io.stalk.xpush.exception.ChannelConnectionException;
import io.stalk.xpush.model.ApplicationInfo;
import io.stalk.xpush.model.Channel;
import io.stalk.xpush.model.Device;
import io.stalk.xpush.model.User;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

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
	
	public static String ERROR_MESSAGE = "message";
	public static String RETURN_STATUS = "status";
	public static String RESULT 		= "result";
	public static String WARN 			= "WARN";
	public static String STATUS_OK 	= "ok";
	
	private static String ACTION_CREATE_CHANNEL 		= "channel-create";
	private static String ACTION_CHANNEL_LIST 			= "channel-list";
	private static String ACTION_ACTIVE_CHANNEL_LIST 	= "channel-list-active";
	public static String ACTION_GET_UNREADMESSAGES 		= "message-unread";
	public static String ACTION_UNREADMESSAGES_RECEIVED	= "message-received";
	public static String ACTION_USER_LIST 				= "user-query";
	public static String ACTION_GET_CHANNEL				= "channel-get";
	public static String ACTION_CHANNEL_EXIT			= "channel-exit";
	public static String ACTION_CHANNEL_JOIN			= "join";
	
	// socket connect options
	private int MAX_CONNECTION = 5;
	private long MAX_TIMEOUT = 30000;	
	
	public ApplicationInfo appInfo;
	public User mUser = new User();
	private ChannelConnection mSessionChannel;
	private ArrayList<JSONObject> mReceiveMessageStack = new ArrayList<JSONObject>();
	private HashMap<String, ChannelConnection> mChannels = new HashMap<String, ChannelConnection>();
	
	private XPushEmitter.messageReceived msgCallback;
	protected Boolean isConnectCallback = false;
	
	private Boolean receivedReady = false;
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
	 * @param userId 	Your user account id in XPush
	 * @param password 	password for account in XPush
	 * @param deviceId 	Your device 
	 * @return login 	result status
	 * @throws AuthorizationFailureException	does not exist user & device , incorrect password  
	 * @throws ChannelConnectionException 
	 */
	public String login(String userId, String password, String deviceId) throws AuthorizationFailureException, ChannelConnectionException{
		JSONObject sendData = new JSONObject();
		try {
			sendData.put( XPushData.APP_ID, this.appInfo.getAppId());
			sendData.put( XPushData.USER_ID, userId);
			sendData.put( XPushData.PASSWORD, password);
			sendData.put( XPushData.DEVICE_ID, deviceId);
			
			JSONObject resultO = asyncCall("auth", "POST", sendData);
			String status = resultO.getString( RETURN_STATUS );
			if("ok".equalsIgnoreCase( resultO.getString( RETURN_STATUS ) ) ){
				mUser = new User(userId, new Device(deviceId) );
				connectSessionSocket(resultO.getJSONObject(RESULT));
				return null;
			}else {
				String errMsg = resultO.getString(ERROR_MESSAGE);
				throw new AuthorizationFailureException(status ,errMsg);
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
	 * @param userId 	Your user account id in XPush
	 * @param password 	password for account in XPush
	 * @param deviceId 	Your device 
	 * @return 
	 * @throws ChannelConnectionException 
	 */
	public void signup(String userId, String password, String deviceId) throws AuthorizationFailureException, ChannelConnectionException{
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
				throw new AuthorizationFailureException(status, error);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * <p>
	 * signup to XPush.
	 * </p>
	 * 
	 * @param userId 	Your user account id in XPush
	 * @param password 	password for account in XPush
	 * @param deviceId 	Your device
	 * @param notiId 	if android device , then this is GCM id  
	 * @return 
	 * @throws ChannelConnectionException 
	 */
	public void signup(String userId, String password, String deviceId, String notiId) throws AuthorizationFailureException, ChannelConnectionException{
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
				throw new AuthorizationFailureException(status, error);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * <p>
	 * Send messages to all users in channel. 
	 * </p>
	 * 
	 * @param chName	target channel name
	 * @param key		send data key
	 * @param value 	send data value
	 * @param cb 		callback when message is sended.
	 */
	public void send(String chName, String key, JSONObject value, Emitter.Listener cb){
		ChannelConnection tCh = null;
		if( mChannels.containsKey(chName) ){
			tCh = mChannels.get(chName);
		}else{
			tCh = createChannel(chName, ChannelConnection.CHANNEL);
			initChannel(chName, tCh);
		}
		tCh.sendMessage(key, value, cb);
		
	}
	
	/**
	 * <p>
	 * Connection static Session server. while internet is connecting, session server keep connect.
	 * Session server receives every notification message. 
	 * </p>
	 * 
	 * @param info (token : server key(String), server: server id(int) , serverUrl : server address(String), 
	 */
	public void connectSessionSocket(JSONObject info){
		mSessionChannel = new ChannelConnection(this, ChannelConnection.SESSION, info);
		try {
			mSessionChannel.connect(null);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	//{"US":[{"D":"WEB","U":"notdol101","N":null},
	//{"D":"WEB","U":"notdol102","N":null}],
	//"_id":"stalk-io^tempChannel","A":"stalk-io","CD":"2014-08-16T06:19:14.136Z","C":"tempChannel","__v":0}
	/**
	 * <p>
	 * Create new channel(room) with other users.
	 * </p>
	 * @param users 	users who join the channel
	 * @param chName 	channel name ( possible empty )
	 * @param datas 	meta data in channel
	 * @param cb 		callback when channel create
	 */
	public void createChannel(String[] users, final String chName, JSONObject datas, final XPushEmitter.createChannelListener cb){
		System.out.println("xpush: createChannel");
		boolean isExistSelf = false;
		JSONArray userList = new JSONArray();
		for( int i = 0 ; i < users.length;i++){
			if(users[i] == mUser.getId()){
				isExistSelf = true;
			}
			userList.put(users[i]);
		}
		
		if(isExistSelf == false) { userList.put( mUser.getId() ); };
		
		JSONObject sendValue = new JSONObject();
		try {
			if(chName != null) sendValue.put(XPushData.CHANNEL_ID, chName);
			sendValue.put(XPushData.USER_ID, userList);			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		final ChannelConnection ch = createChannel(chName, ChannelConnection.CHANNEL);
		
		this.sEmit(ACTION_CREATE_CHANNEL, sendValue, new Emitter.Listener() {
			
			public void call(Object... args) {
				System.out.println("xpush : createChannel(receive)");
				String status = (String)args[0];
				
				if(args[0] != null && ChannelConnectionException.STATUS_CHANNEL_EXIST.equalsIgnoreCase(status)){
					System.out.println("xxxxx xpush: createChannel " + args[0]);
					String errMsg = (String)args[1];
					cb.call(new ChannelConnectionException(status, errMsg),null,null,null);
					return;
				}
				
				JSONObject result = (JSONObject)args[1];
				List<User> channelUsers = new ArrayList<User>();
				String realChName = chName;
				try {
					if(chName == null){
						realChName = result.getString( XPushData.CHANNEL_ID );
						registChannel( realChName , ch);
					}
					initChannel(realChName, ch);
					//JSONObject result2 = getChannelInfo(realChName);
					//ch.setServerInfo(result2.getJSONObject(RESULT));
					//ch.connect(null);
					
					JSONArray rUsers = result.getJSONArray(XPushData.USER_IDS);
					for(int i = 0 ; i<rUsers.length(); i++){
						JSONObject userO = rUsers.getJSONObject(i);
						User user = new User(userO.getString(XPushData.USER_ID), new Device(userO.getString(XPushData.DEVICE_ID)) );
						channelUsers.add(user);
					}
					
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				cb.call(null, realChName, ch, channelUsers);
			}
		});
	}
	
	private void initChannel(String chNm , ChannelConnection ch){
		JSONObject result;
		try {
			result = getChannelInfo(chNm);
			ch.setServerInfo(result.getJSONObject(RESULT));
			ch.connect(null);
		} catch (ChannelConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @param chName	channel name that created channel
	 * @param mode		SESSION OR CHANNEL
	 * @return
	 */
	private ChannelConnection createChannel(String chName, String mode){
		ChannelConnection rCh = new ChannelConnection(this,mode);
		
		if(chName != null){
			registChannel(chName, rCh);
		}
		return rCh;
	}
	
	/**
	 * <p>
	 * get {@link io.stalk.xpush.ChannelConnection} class.
	 * </p>
	 * 
	 * @param chName channel name what you want 
	 * @return if channel is exist then ChannelConnection class, if doesn't then null
	 */
	public ChannelConnection getChannel(String chName){
		if(mChannels.containsKey(chName)){
			return mChannels.get(chName);
		}else{
			return null;
		}
	}
	
	/**
	 * <p>
	 *  Add new ChannelConnection class to XPush class
	 * </p>
	 * @param chName 	channel name
	 * @param ch		{@link io.stalk.xpush.ChannelConnection}
	 */
	private void registChannel(String chName, ChannelConnection ch){
		mChannels.put(chName, ch);
	}
	
	/**
	 * <p>
	 * Get channel list on login user.
	 * </p>
	 * @param cb	callback when channel list is received.
	 */
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
							Channel receivedChannel = new Channel();
							
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
	
	/**
	 * <p>
	 * Get Active(user is connected in site) channel list. 
	 * </p>
	 * 
	 * @param data	{"key" : "data"} execute query in Active channels.(options)
	 * @param cb	callback when active channel is received.
	 */
	public void getChannelsActive(JSONObject data, Emitter.Listener cb){
		// data.key (options)
		this.sEmit(ACTION_ACTIVE_CHANNEL_LIST, new JSONObject(), cb);
		// app, channel, created
		// todo : return type is object.
	}
	
	/**
	 * <p>
	 * Get channel server information for connect to server.
	 * </p>
	 * @param chNm	channel name 
	 * @return
	 * @throws ChannelConnectionException 
	 */
	public JSONObject getChannelInfo(String chNm) throws ChannelConnectionException{
		//{"result":{"seq":"WJ5hNWpaZ","server":{"name":"23","channel":"tempChannel","url":"http://192.168.0.6:9991"},"channel":"tempChannel"},"status":"ok"}	
		return asyncCall( "node/"+ this.appInfo.getAppId() + '/' + chNm, "GET", new JSONObject());
	}
	
	/**
	 * <p>
	 * Exist channel. 
	 * </p>
	 * @param chNm	channel name 
	 * @return
	 * @throws ChannelConnectionException 
	 */
	public void exitChannel(String chNm, final Emitter.Listener cb) throws ChannelConnectionException{
		JSONObject query = new JSONObject();
		try {
			query.put( XPushData.CHANNEL_ID , chNm);
			
			this.sEmit(ACTION_CHANNEL_EXIT, query, new Emitter.Listener() {
				
				public void call(Object... args) {
					// TODO Auto-generated method stub
					System.out.println("***** channel exit");
					System.out.println(args[0]);
					cb.call(args);
				}
			});
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * <p>
	 * Invite other users in this channel. 
	 * </p>
	 * @param chNm		channel name 
	 * @param userId 	user who invite in this channel
	 * @return
	 */	
	public void joinChannel(String chNm, String userId, final Emitter.Listener cb) {
		ChannelConnection ch = getChannel(chNm);
		
		JSONObject params = new JSONObject();
		try {
			params.put( XPushData.USER_ID , userId);
			if(ch == null){
				ch = createChannel(chNm, ChannelConnection.CHANNEL);
				initChannel(chNm, ch);
				//JSONObject result = getChannelInfo(chNm);
				//ch.setServerInfo(result.getJSONObject(RESULT));
				//ch.connect(null);
				
				ch.send( ACTION_CHANNEL_JOIN, params, new Emitter.Listener() {
					public void call(Object... args) {
						JSONObject result = (JSONObject)args[0];
						String status;
						try {
							status = result.getString( RETURN_STATUS );
							
							System.out.println("*&*&*&*&*&*&*&*&*&*&*& "+ status);
							if(STATUS_OK.equalsIgnoreCase(status)){
								cb.call(null);
							}else{
								String message = result.getString(ERROR_MESSAGE);
								cb.call(status,message);
							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * <p>
	 * Get User List in this Application(XPush). and first param is exist for pagination.
	 * </p>
	 * 
	 * @param params(option)	{query: "(String)", column : "(JSONObject)", options: "(JSONObject)"} paging option. if this is null , get all users.
	 * @param cb				callback when user list is received.
	 */
	public void getUserList(JSONObject params, final XPushEmitter.receiveUserList cb){
		if(params == null ) params = new JSONObject();
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
		try {
			if(params.isNull("query") ) params.put("query", new JSONObject());
			if(params.isNull("column") ) params.put("column", new JSONObject().put("U",1).put("DT",1).put("_id",0));
			if(params.isNull("options") ) params.put("options", new JSONObject().put("skipCount",true).put("sortBy",new JSONObject().put("DT.NM", 1)));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(params);
		this.sEmit(ACTION_USER_LIST, params == null ? new JSONObject() : params , new Emitter.Listener() {
			
			public void call(Object... args) {
				
				System.out.println("************** "+args[0]);
				System.out.println("************** "+args[1]);
				
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
						
						//if(userO.getString(XPushData.DATA) != null || userO.getString(XPushData.DATA).equalsIgnoreCase("null") ){
						if ( !userO.isNull(XPushData.DATA) ){
							rUser.setData(userO.getJSONObject(XPushData.DATA));
						}
						receivedUsers.add(rUser);
					}
					
						cb.call(status, receivedUsers);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}else{
					cb.call(status,null);
				}
			}
		});
	}

	/**
	 * <p>
	 * Get User List in channel. and second param is exist for pagination.
	 * </p>
	 * @param channelName	channel Name for search user in same channel. 
	 * @param params		{column : "(JSONObject)", options: "(JSONObject)"} paging option. if this is null , get all users.
	 * @param cb			callback when user list is received.
	 */
	public void getUserListInChannel(String channelName, final XPushEmitter.receiveUserList cb){
		try {
			JSONObject query = new JSONObject();
			query.put( XPushData.CHANNEL_ID , channelName);
			
			this.sEmit(ACTION_GET_CHANNEL, query, new Emitter.Listener() {
				
				public void call(Object... args) {
					// TODO Auto-generated method stub
					
					if(args[0] == null){
						List<User> returnUsers = new ArrayList<User>();
						try {
							JSONArray users = ((JSONObject)args[1]).getJSONArray( XPushData.USER_IDS );
							for(int i = 0 ; i < users.length() ; i ++){
								JSONObject uO = (JSONObject)users.get(i);
								User user = new User(uO.getString(XPushData.USER_ID), new Device(uO.getString(XPushData.DEVICE_ID)) );
								returnUsers.add(user);
							}
							
							cb.call(null,returnUsers);
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else{
						cb.call(args[0].toString(), null);
					}
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * <p>
	 * Ajax call intermediary.
	 * </p>
	 * 
	 * @param context	server context path
	 * @param method	REST METHOD(GET,POST,PUT,DEL)
	 * @param sendData	send data object
	 * @return
	 * @throws ChannelConnectionException 
	 */
	public JSONObject asyncCall(String context, String method, JSONObject sendData ) throws ChannelConnectionException{
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
		} catch (FileNotFoundException e){
			e.printStackTrace();
			throw new ChannelConnectionException("404", "XPush Session server doesn't exist!!!");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		System.out.println("==== status : "+status);
		return new JSONObject();
	}
	

	/**
	 * <p>
	 * Send data to Session server in socket protocol(intermediary). data is consists of key-value pair.
	 * </p>
	 * 
	 * @param key		send data key
	 * @param value		send data value
	 * @param cb		callback when message is send.
	 */
	public void sEmit(final String key, JSONObject value, final Emitter.Listener cb){
		
		this.mSessionChannel.send(key, value, new Emitter.Listener() {
			
			public void call(Object... args) {
				String status;
				String message;
				try {
					JSONObject result = null;
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
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Keep received message, when socket is connected , old message flush.
	 */
	public void receivedMessageFlush(){
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ receivedMessageFlush : "+mReceiveMessageStack.size());
		try {
			while(mReceiveMessageStack.size() > 0 ){
				JSONObject message = mReceiveMessageStack.remove(0);
				//this.emit(message.getString("EVENT"), message.get("ARGS"));
				Object[] args = (Object[])message.get("ARGS");
				String chNm = (String)args[0];
				String key = (String)args[1];
				JSONObject dt = (JSONObject)args[2];
				this.msgCallback.call(chNm,key,dt);
			}
		} catch (JSONException e) { 
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//receivedReady = true;
	}
	
    /* (non-Javadoc)
     * Overwrite (@link com.github.nkzawa.emitter.Emitter#emit) method. This emit data.
     * 
     * @see com.github.nkzawa.emitter.Emitter#emit(java.lang.String, java.lang.Object[])
     */
    public Emitter emit(String event, Object... args) {
    	if(isExistUnread == true || msgCallback == null){
    		JSONObject newEvent = new JSONObject();
    		try {
				newEvent.put("EVENT", event);
	    		newEvent.put("ARGS", args);				
			} catch (JSONException e) {
				e.printStackTrace();
			}
    		mReceiveMessageStack.add(newEvent);
    	}else{
    		if("message".equalsIgnoreCase(event)){
    			
            	String chNm = (String)args[0];
            	String key = (String)args[1];
            	JSONObject dt = (JSONObject)args[2];
            	msgCallback.call(chNm, key, dt);
    		}else{
    			super.emit(event, args);
    		}
    	}
        return this;
    }
    
    public void onMessageReceived(XPushEmitter.messageReceived fn){
    	msgCallback = fn;
    	mSessionChannel.getUnreadMessagesAndEmit(null);
    	isConnectCallback = true;
    }
    
    public void offMessageReceived(){
    	msgCallback = null;
    	isConnectCallback = false;
    }
    
    public void disconnect(){
    	if(mSessionChannel != null){
    		mSessionChannel.disconnect();
    	}
    	
    	Set<String> keys = mChannels.keySet();
    	Iterator<String> keysIter = keys.iterator();
    	
    	while(keysIter.hasNext()){
    		String chNm = keysIter.next();
    		ChannelConnection ch = getChannel(chNm);
    		if(ch != null) ch.disconnect();
    	}
    }
}
