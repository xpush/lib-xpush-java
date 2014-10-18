package io.stalk.xpush.user;

import io.stalk.xpush.XPush;
import io.stalk.xpush.XPushEmitter;
import io.stalk.xpush.exception.AuthorizationFailureException;
import io.stalk.xpush.exception.ChannelConnectionException;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import com.github.nkzawa.emitter.Emitter;

public class LiveMessage {
	//public static final String host = "http://stalk-front-s01.cloudapp.net:8000";
	public static final String host = "http://www.notdol.com:8000";
	public static final String appId = "stalk-io";
	
	private XPush xpush = null;
	
	private int msgCount = 1;
	
	@Test
	public void sendMessageInterval(){
		Timer timer = new Timer(true);
		
		xpush = new XPush(host, appId);
		
		String userId = "notdol301";
		String password = "win1234";
		String deviceId = "testunit";
		final String channelId = "WkUHNj364";
		
		try {
			xpush.login(userId, password, deviceId);
			xpush.onMessageReceived(new XPushEmitter.messageReceived() {
				
				@Override
				public void call(String channelName, String key, JSONObject value) {
					// TODO Auto-generated method stub
					System.out.println(channelName +":"+ key +":"+ value);
				}
			});
			
			/*
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					JSONObject sendObj = new JSONObject();
					try {
						sendObj.put("key", "value");
						sendObj.put("text", "this is message from others : "+ plusIndex());
						sendObj.put("sender", xpush.mUser.getId());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					xpush.send(channelId, "message", sendObj, new Emitter.Listener() {
						@Override
						public void call(Object... args) {
							// TODO Auto-generated method stub
							
						}
					});
				}
			}, 0, 3000);*/
			
		} catch (AuthorizationFailureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ChannelConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        try {
            //assuming it takes 20 secs to complete the task
            Thread.sleep(500000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
		
	}
	
	private int plusIndex(){
		return msgCount++;
	}
	
}
