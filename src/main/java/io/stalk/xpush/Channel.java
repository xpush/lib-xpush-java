package io.stalk.xpush;

import java.net.URISyntaxException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.JsonObject;

public class Channel {

	public static String SESSION = "session";
	public static String CHANNEL = "channel";
	public static String CHANNEL_ONLY = "CHANNEL_ONLY";
	public static String KEY = "NM";
	public static String DATA = "DT";
	public static String CALLBACK = "CB";
	public static String JOIN = "join";

	
	private Socket _socket;	
	private IO.Options socketOptions;
	private ArrayList<JSONObject> sendMessages = new ArrayList<JSONObject>();
	private Boolean _isConnected =false;
	private Boolean _isFirtConnect = false;
	
	private XPush _xpush;
	private String _type = CHANNEL;
	private JsonObject _info; 
	private final Channel self = this;
	
	private ArrayList<JsonObject> _messageStack;

	public Channel(XPush xpush){
		this._xpush = xpush;
	}
	
	public Channel(XPush xpush, String type, JsonObject info){
		this._xpush = xpush;
		this._type = type;
		this._info = info;
		
		_messageStack = new ArrayList<JsonObject>();
		
		socketOptions = new IO.Options();
		socketOptions.forceNew = true;
		socketOptions.reconnection = false;
	}

	public void setServerInfo(JsonObject info, Emitter.Listener cb){
		this._info = info;
		
		
	}
	
	public void connect(String mode){
		//http://www.notdol.com:9992/session?A=stalk-io&U=notdol110&D=WEB&TK=JZTbSCT8mN 
		String query;
		try {
			query = "A="+_xpush.appInfo.getAppId()+"&"+"U="+ _xpush.mUser.getUserId() +"&"+"D="+ _xpush.mUser.getDeviceId() +"&"+
		        "TK="+ _info.get("token").getAsString();

		    if(this._type == CHANNEL){
			query = "A="+_xpush.appInfo.getAppId()+"&"+"U="+ _xpush.mUser.getUserId() +"&"+"D="+ _xpush.mUser.getDeviceId() +"&"+
			        "TK="+ _info.get("token").getAsString()+"&"+"S="+_info.get("server").getAsString()+
		        "C="+ "{channel}";

		      if(mode != null){
		        if(mode == CHANNEL_ONLY){
		          this._xpush.isExistUnread = false;
		        }
		        query = query +"&MD="+ mode;
		      }
		    }
		    
			this._socket = IO.socket(_info.get("serverUrl").getAsString()+"/"+ this._type+"?"+query, socketOptions);

		    System.out.println( "xpush : socketconnect " + _info.get("serverUrl").getAsString()+"/"+ this._type+"?"+query);
		    this._socket.on(Socket.EVENT_ERROR, new Emitter.Listener() {
				
				public void call(Object... arg0) {
					// TODO Auto-generated method stub
				      System.out.println( "channel connection error" );
				}
			});
		    
		    this._socket.on(Socket.EVENT_CONNECT,new Emitter.Listener() {
				public void call(Object... arg0) {
					// TODO Auto-generated method stub
				      System.out.println( self._type+ " connection completed" );
				      
						JSONObject message;
						while(sendMessages.size() >0){
							System.out.println("====== start");
							message = sendMessages.remove(0);
							System.out.println("======== "+message);
							try {
								System.out.println( message.get(CALLBACK) );
								final Emitter.Listener cb = (Emitter.Listener)message.get(CALLBACK);
								self._socket.emit( message.getString(KEY) ,  message.getJSONObject(DATA) ,new Ack() {
									
									public void call(Object... arg0) {
										// TODO Auto-generated method stub
										System.out.println("-======== emit return");
										cb.call(arg0);
									}
								});
							
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
				      
				      afterConnectSocket();
				}
		    });
		    
		    this._socket.on(Socket.EVENT_DISCONNECT,new Emitter.Listener() {
				public void call(Object... arg0) {
					// TODO Auto-generated method stub
				      System.out.println( "channel disconnection completed" );
				      
				      while(_messageStack.size() > 0 ){
				    	  JsonObject t = _messageStack.remove(0);
				    	  _socket.emit("send", t, new Ack() {
							
							public void call(Object... arg0) {
								// TODO Auto-generated method stub
								
							}
						});
				      }
				      
				      _isConnected = false;
				      if(!_isFirtConnect) return;
				      _isFirtConnect = false;
				      
				      afterConnectSocket();
				}
		    });
		    
			this._socket.connect();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void afterConnectSocket(){
		_isConnected = true;
	}
	
	private void getUnreadMessages(){
		
	}
	
	
	public void send(String key, JSONObject value, Emitter.Listener cb){
		if( _isConnected ){
			this._socket.emit(key,  value.toString(), new Ack() {
				
				public void call(Object... arg0) {
					// TODO Auto-generated method stub
					System.out.println("====== send");
				}
			});
		}else{
			/*
			JsonObject newMessage = new JsonObject();
			newMessage.addProperty(KEY, key);
			newMessage.add(DATA, value);
			*/
			System.out.println("===== add stack");
			JSONObject newMsg = new JSONObject();
			try {
				newMsg.put(KEY, key);
				newMsg.put(DATA, value);
				newMsg.put(CALLBACK, cb);
				this.sendMessages.add(newMsg);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void disconnect(){
		this._socket.disconnect();
	}
	
	public void joinChannel(JsonObject param, Emitter.Listener cb){
		if(this._isConnected){
			this._socket.emit(JOIN, param.toString(), cb );
		}
	}
	
	
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
