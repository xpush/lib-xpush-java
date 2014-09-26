package io.stalk.xpush;

import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.management.timer.Timer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * <p>
 * This class is the main entry point for accessing Channel Server(XPush).
 * Channel server is dynamically assigned. If assigned channel server is terminate, connection is closed and other server is assigned.
 * </p>
 * 
 * @author XPush
 *
 */
public class ChannelConnection {

	public static String SESSION = "session";
	public static String CHANNEL = "channel";
	public static String CHANNEL_ONLY = "CHANNEL_ONLY";
	public static String JOIN = "join";
	public static String SEND_KEY = "send";
	public static String RECEIVE_KEY = "message";
	public static String SYSTEM_RECEIVE_KEY = "system";
	
	// session event 
	private static String SESSION_EVENT_KEY = "_event";
	private static String SESSION_RESULT_EVENT_KEY = "event";
	private static String SESSION_EVENT_NOTIFICATION = "NOTIFICATION";
	private static String SESSION_EVENT_CONNECT = "CONNECT";
	private static String SESSION_EVENT_DISCONNECT = "DISCONNECT";
	private static String SESSION_EVENT_LOGOUT = "LOGOUT";
	
	public static final String TOKEN = "token";
	public static final String SERVER = "server";
	public static final String SERVER_URL = "serverUrl";

	private Socket _socket;
	private IO.Options socketOptions;
	private ArrayList<JSONObject> sendMessages = new ArrayList<JSONObject>();
	
	private XPush _xpush;
	private String _type = CHANNEL;
	public String name;
	private JSONObject _info;
	private final ChannelConnection self = this;
	
	private ArrayList<JsonObject> _messageStack;

	private Boolean _isConnected = false;
	private Boolean _isFirtConnect = false;
	
	/**
	 * <p>
	 * Create a new instance of XPush With XPush.
	 * </p>
	 * 
	 * @param xpush	{@link io.stalk.xpush.XPush} 
	 */
	public ChannelConnection(XPush xpush){
		this._xpush = xpush;
		_messageStack = new ArrayList<JsonObject>();
		
		socketOptions = new IO.Options();
		socketOptions.forceNew = true;
		socketOptions.reconnection = false;
	}
	
	/**
	 * <p>
	 * Create a new instance of ChannelConnection with XPush,connectionType.
	 * </p>
	 * 
	 * @param xpush {@link io.stalk.xpush.XPush}
	 * @param type	Connection type for channelserver. ( SESSION, CHANNEL, CHANNEL_ONLY ) 
	 */
	public ChannelConnection(XPush xpush, String type){
		this(xpush);
		this._type = type;
	}
	
	/**
	 * <p>
	 * Create a new instance of ChannelConnection with XPush,connectionType.
	 * </p>
	 * 
	 * @param xpush {@link io.stalk.xpush.XPush}
	 * @param type	Connection type for channelserver. ( SESSION, CHANNEL, CHANNEL_ONLY ) 
	 * @param info	Channel Server information for connect assigned server.
	 */
	public ChannelConnection(XPush xpush, String type, JSONObject info){
		this(xpush, type);
		this._info = info;
	}

	/**
	 * @param info	Channel Server information for connect assigend server.
	 */
	public void setServerInfo(JSONObject info){
		this._info = info;
	}
	
	/**
	 * <p>
	 * Get server address to connect server.
	 * </p>
	 * 
	 * @return	server address
	 */
	private String getServerUrl(){
		try {
			if(this._type == SESSION){
				return this._info.getString(SERVER_URL);
			}else{
				return this._info.getJSONObject(SERVER).getString("url");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * <p>
	 * Connect Session or Channel server. 
	 * </p>
	 * 
	 * @param mode			connection mode ( SESSION, CHANNEL, CHANNEL_ONLY )
	 * @throws JSONException
	 */
	public void connect(String mode) throws JSONException{
		String query;
		try {
		    if(this._type == CHANNEL){
				query = "A="+_xpush.appInfo.getAppId()+"&"+
						"U="+ _xpush.mUser.getId() +"&"+
						"D="+ _xpush.mUser.getActiveDevice().getDeviceId() +"&"+
				        /*"TK="+ _info.getString(TOKEN)+"&"+*/"S="+_info.getJSONObject(SERVER).getString("name")+"&"+
				        "C="+ _info.getString(CHANNEL);
				if(mode != null){
					if(mode == CHANNEL_ONLY){
						this._xpush.isExistUnread = false;
					}
					query = query +"&MD="+ mode;
				}
		    }else{
				query = "A="+_xpush.appInfo.getAppId()+"&"+
						"U="+ _xpush.mUser.getId() +"&"+
						"D="+ _xpush.mUser.getActiveDevice().getDeviceId() +"&"+
				        "TK="+ _info.getString( TOKEN );
		    }
		    
		    System.out.println("channel: connect "+getServerUrl()+"/"+ this._type+"?"+query);
			this._socket = IO.socket( getServerUrl() +"/"+ this._type+"?"+query, socketOptions);

		    this._socket.on(Socket.EVENT_ERROR, new Emitter.Listener() {
				
				public void call(Object... arg0) {
				      System.out.println( "channel connection error" );
				}
			});
		    
		    this._socket.on(Socket.EVENT_CONNECT,new Emitter.Listener() {
				public void call(Object... arg0) {
				      System.out.println("channel: connect(receive)  "+ self._type+ " connection completed" );
				      
						self.getUnreadMessages(new Emitter.Listener() {
							
							public void call(Object... arg0) {
								// TODO Auto-generated method stub
								System.out.println("channel: getUnreadMessages(receive) ");
								String err = (String)arg0[0];
								JSONArray messages = (JSONArray)arg0[1];
								System.out.println(messages);
								JSONObject message, insertMessage, mg;
								try {
								
									for(int i = messages.length()-1; i > 0 ; i--){
											message = messages.getJSONObject(i);
											ArrayList<Object> arr = new ArrayList<Object>();
											insertMessage = new JSONObject();
											String chNm = (new JSONObject( message.getJSONObject(XPushData.MESSAGE).getString(XPushData.DATA) )).getString(XPushData.CHANNEL_ID);
											arr.add(chNm);
											arr.add(message.getString(XPushData.NAME));
											arr.add(message.getJSONObject(XPushData.MESSAGE).getString(XPushData.DATA));
											
											insertMessage.put("EVENT", RECEIVE_KEY);
									}
									
								} catch (JSONException e) {
									e.printStackTrace();
								}
								self._xpush.receivedMessageFlush();
							}
						});
						
						JSONObject message;
						while(sendMessages.size() >0){
							message = sendMessages.remove(0);
							System.out.println("channel : sendMessages "+message);
							try {
								final Emitter.Listener cb = (Emitter.Listener)message.get(XPushData.CALLBACK);
								JSONObject data = null;
								if(message.has(XPushData.DATA)){
									data = message.getJSONObject(XPushData.DATA);
								};
								self.realSend(message.getString(XPushData.NAME) , data, cb);
									/*
									self._socket.emit( message.getString(KEY) ,  message.getJSONObject(DATA) ,new Ack() {
										public void call(Object... arg0) {
											// TODO Auto-generated method stub
											System.out.println("-======== emit return");
											cb.call(arg0);
										}
									});
									*/
									
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
				      
				      if(SESSION.equalsIgnoreCase(_type)){
				    	  afterConnectSessionSocket();
				      } else{
				    	 afterConnectSocket();
				      }
				}
		    });
		    
		    this._socket.on(Socket.EVENT_DISCONNECT,new Emitter.Listener() {
				public void call(Object... arg0) {
				      System.out.println( "channel: disconnect" );
				      
				      while(_messageStack.size() > 0 ){
				    	  JsonObject t = _messageStack.remove(0);
				    	  _socket.emit("send", t, new Ack() {
							
							public void call(Object... arg0) {
							}
						});
				      }
				      
				      _isConnected = false;
				      if(!_isFirtConnect) return;
				      _isFirtConnect = false;
				      
				      //afterConnectSocket();
				}
		    });
		    
			this._socket.connect();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * <p>
	 * Session server is connected, then post process is executed.
	 * Ready for all notification receive.
	 * </p>
	 */
	private void afterConnectSessionSocket(){
		_isConnected = true;

		System.out.println("channel: afterConnectSessionSocket");
		this._socket.on(SESSION_EVENT_KEY, new Emitter.Listener() {
			
			public void call(Object... args) {
				System.out.println("channel: session event"+ args);
				//{"event":"NOTIFICATION","C":"byuEd760b","NM":"testkey","DT":{"C":"byuEd760b","TS":1408287751923},"TS":1408287751923}
				JSONObject result = (JSONObject)args[0];
				String event;
				try {
					event = result.getString(SESSION_RESULT_EVENT_KEY);
					if(event.equals( SESSION_EVENT_NOTIFICATION )){
						String chNm = result.getString( XPushData.CHANNEL_ID );
						String name = result.getString( XPushData.NAME );
						JSONObject dt = result.getJSONObject( XPushData.DATA );
						
						self._xpush.emit( RECEIVE_KEY , chNm, name, dt);
						
					}else if(event.equals( SESSION_EVENT_CONNECT )){
						self._xpush.emit("___session_event", SESSION , result);
					}else if(event.equals( SESSION_EVENT_DISCONNECT )){
						self._xpush.emit("___session_event", SESSION , result);
					}else if(event.equals( SESSION_EVENT_LOGOUT )){
						self._xpush.emit("___session_event", event, result);
					}
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * <p>
	 * Channel server is connected, then post process is executed.
	 * Ready for all message receive.
	 * </p>
	 */
	private void afterConnectSocket(){
		_isConnected = true;
		
	    this._socket.on( RECEIVE_KEY, new Emitter.Listener() {
			
			public void call(Object... args) {
				System.out.println("channel: channel receive "+name);
				self._xpush.emit(RECEIVE_KEY, name, RECEIVE_KEY, args[0] );
			}
		});
	    		
	    this._socket.on( SYSTEM_RECEIVE_KEY, new Emitter.Listener() {
			
			public void call(Object... args) {
				System.out.println("channel: channel system receive "+name);
				self._xpush.emit(RECEIVE_KEY, name, RECEIVE_KEY, args[0] );
			}
		});
	    /*
	      if(self._xpush._isEventHandler) {
	        self._socket.on('_event',function(data){

	          switch(data.event){
	            case 'CONNECTION' :
	              self._xpush.emit('___session_event', 'CHANNEL', data);
	            break;
	            case 'DISCONNECT' :
	              self._xpush.emit('___session_event', 'CHANNEL', data);
	            break;
	          }
	        });
	      }
	      if(cb)cb();
	    */
	}
	
	/**
	 * <p>
	 * When socket is connected, for unread messages. 
	 * </p>
	 * 
	 * @param cb	callback when message is received.
	 */
	private void getUnreadMessages(final Emitter.Listener cb){
		self._xpush.sEmit( XPush.ACTION_GET_UNREADMESSAGES , null , cb);
		//self.realSend(XPush.ACTION_GET_UNREADMESSAGES, null, cb);
	}
	
	/**
	 * <p>
	 * If socket is not connected, developer is trying to send messages. XPush keep this messages. 
	 * This method is called when socket is connected. 
	 * </p>
	 * 
	 * @param key	send data (key)
	 * @param value	send data (value)
	 * @param cb	callback when message is send.
	 */
	private void realSend(final String key, JSONObject value, final Emitter.Listener cb ){
		if(value == null){
			this._socket.emit(key, new Ack() {
				public void call(Object... arg0) {
					System.out.println("channel: realSend1 "+key);
					cb.call(arg0);
				}
			});
		}else{
			this._socket.emit(key,  value, new Ack() {
				public void call(Object... arg0) {
					System.out.println("channel: realSend2 "+key);
					cb.call(arg0);
				}
			});
		}
	}

	/**
	 * <p>
	 * If socket is not connected, developer is trying to send messages. XPush keep this messages. 
	 * This method keep messages when socket is not connected. Socket is connected then call readSend method(private). 
	 * </p>
	 * 
	 * @param key	send data (key)
	 * @param value	send data (value)
	 * @param cb	callback when message is send.
	 */
	public void send(String key, JSONObject value, final Emitter.Listener cb){
		if( _isConnected ){
			this.realSend(key,value,cb);
		}else{
			System.out.println("channel: send stack");
			JSONObject newMsg = new JSONObject();
			try {
				newMsg.put(XPushData.NAME, key);
				newMsg.put(XPushData.DATA, value);
				newMsg.put(XPushData.CALLBACK, cb);
				this.sendMessages.add(newMsg);
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * <p>
	 * If socket is not connected, developer is trying to send messages. XPush keep this messages. 
	 * This method keep messages when socket is not connected. Socket is connected then call readSend method(private). 
	 * </p>
	 * 
	 * @param key	send data (key)
	 * @param value	send data (value)
	 * @param cb	callback when message is send.
	 */
	public void sendMessage(String key, JSONObject value, final Emitter.Listener cb){
		JSONObject dataMsg = new JSONObject();
		try{
			dataMsg.put(XPushData.NAME, key);
			dataMsg.put(XPushData.DATA, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if( _isConnected ){
			this._socket.emit(SEND_KEY, dataMsg, new Ack() {
				
				public void call(Object... arg0) {
					// TODO Auto-generated method stub
					System.out.println("====== send");
					cb.call();
				}
			});
		}else{
			System.out.println("channel: send stack");
			JSONObject newMsg = new JSONObject();
			try {
				newMsg.put(XPushData.NAME, SEND_KEY);
				newMsg.put(XPushData.DATA, dataMsg);
				newMsg.put(XPushData.CALLBACK, cb);
				this.sendMessages.add(newMsg);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * <p>
	 * Disconnect socket protocol.
	 * </p>
	 */
	public void disconnect(){
		this._socket.disconnect();
	}
	
	/**
	 * @param param
	 * @param cb
	public void joinChannel(JsonObject param, Emitter.Listener cb){
		if(this._isConnected){
			this._socket.emit(JOIN, param.toString(), cb );
		}
	}
	*/
	
	public void on(final String key){
		if(this._isConnected){
			this._socket.on(key, new Emitter.Listener() {
				
				public void call(Object... args) {
					// TODO Auto-generated method stub
					self._xpush.emit(key, args);
				}
			});
		}
	}
}
