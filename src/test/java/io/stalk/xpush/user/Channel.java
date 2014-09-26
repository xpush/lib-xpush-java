package io.stalk.xpush.user;


import java.util.List;

import io.stalk.xpush.ChannelConnection;
import io.stalk.xpush.XPush;
import io.stalk.xpush.XPushEmitter;
import io.stalk.xpush.exception.AuthorizationFailureException;
import io.stalk.xpush.exception.ChannelConnectionException;
import io.stalk.xpush.model.User;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.github.nkzawa.emitter.Emitter;
import com.google.gson.JsonObject;


public class Channel {
	
	private String host = "http://www.notdol.com:8000";
	private String appId = "stalk-io";
	
/*	
	@Test
	public void getChannels() throws InterruptedException{
		final XPush xpush = new XPush(host, appId);
		String returnLogin = null;
		String userId = "notdol3001";
		String password = "win1234";
		String deviceId = "LG-F320L-0168B1456111AB4C";
		
		try {
			returnLogin = xpush.login(userId, password, deviceId);
		} catch (AuthorizationFailureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			if(AuthorizationFailureException.STATUS_USER_NOT_EXIST.equalsIgnoreCase(e.getStatus())){
				System.out.println(e.getMessage()+": userId - "+userId+"  === deviceId - "+deviceId);
			}else if(AuthorizationFailureException.STATUS_ERROR_PASSWORD.equalsIgnoreCase(e.getStatus())){
				System.out.println(e.getMessage());
			}
		}
    	System.out.println(returnLogin);
    	Assert.assertEquals(null, returnLogin);

    	xpush.getChannels(new XPushEmitter.receiveChannelList() {
			public void call(String err,
					List<Channel> channels) {
				// TODO Auto-generated method stub
				System.out.println("=================== received channel list");
				System.out.println(channels);
				
			}
		});
    	Thread.sleep(5000);
	}
*/
	
/*
    @Test                                                         
    public void loginAndConnect() throws InterruptedException{
    	final XPush xpush = new XPush(host, appId);
    	String returnLogin = xpush.login("notdol101", "win1234", "WEB");
    	System.out.println(returnLogin);
    	Assert.assertEquals(null, returnLogin);   
    	xpush.createChannel( new String[]{"notdol102"}, "tempChannel", new JsonObject(), new Emitter.Listener() {
			
			public void call(Object... arg0) {
				// TODO Auto-generated method stub
				System.out.println("===== create channel callback");
				xpush.send("tempChannel", "testkey", new JSONObject(), new Emitter.Listener() {
					
					public void call(Object... arg0) {
						// TODO Auto-generated method stub
						System.out.println("============ send message complete");
					}
				});
			}
		});
    	Thread.sleep(100000);
    }
*/

/*
    @Test
    public void createChannelWithNoName() throws InterruptedException{
    	final XPush xpush = new XPush(host, appId);
    	String returnLogin = null;
		try {
			returnLogin = xpush.login("notdol101", "win1234", "WEB");
		} catch (AuthorizationFailureException e) {
			e.printStackTrace();
		} catch (ChannelConnectionException e){
			e.printStackTrace();
		}
    	System.out.println(returnLogin);
    	Assert.assertEquals(null, returnLogin);
    	
    	xpush.createChannel( new String[]{"notdol102"}, null, new JSONObject(), new XPushEmitter.createChannelListener() {

			public void call(ChannelConnectionException e, String channelName,
					ChannelConnection ch, List<User> users) {
				
				System.out.println("===== create channel callback");
				System.out.println("channel name : "+ channelName);
				Assert.assertNotNull("channel name is empty", channelName);
				Assert.assertNotNull("channel object is null", ch);
				Assert.assertNotNull("users in channel is null", users);
				Assert.assertTrue("user count in channel is wrong",users.size() > 0 );
				
				System.out.println(users);
				sameChannelNameError(xpush, channelName);
			}
		});
    	Thread.sleep(5000);
    }
*/

/*
    private void sameChannelNameError(XPush xpush,String channelName){
    	xpush.createChannel( new String[]{"notdol30001"}, channelName, new JSONObject(), new XPushEmitter.createChannelListener() {
			public void call(ChannelConnectionException e, String channelName,
					ChannelConnection ch, List<User> users) {
				if(e != null){
					System.out.println("****************** "+e.getStatus()+ " : "+e.getMessage());
				}
				
				System.out.println("===== create channel callback");
				System.out.println("channel name : "+ channelName);
			}
		});
    }
*/
	
    @Test
    public void createChannelWithNoNameAndSend() throws InterruptedException{
    	final XPush xpush = new XPush(host, appId);
    	final XPush xpush2 = new XPush(host, appId);
    	
		try {
			String returnLogin = xpush.login("notdol101", "win1234", "WEB");
	    	System.out.println(returnLogin);
	    	Assert.assertEquals(null, returnLogin);   
	    	String returnLogin2 = xpush2.login("notdol102", "win1234", "WEB");
	    	System.out.println(returnLogin2);
	    	Assert.assertEquals(null, returnLogin2);   
			
		} catch (AuthorizationFailureException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ChannelConnectionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	
    	xpush.createChannel( new String[]{"notdol102"}, null, new JSONObject(), new XPushEmitter.createChannelListener() {
				
				public void call(ChannelConnectionException e, String channelName,
						ChannelConnection ch, List<User> users) {
				// TODO Auto-generated method stub
				
				xpush2.on( ChannelConnection.RECEIVE_KEY ,new Emitter.Listener() {
					
					public void call(Object... args) {
						// TODO Auto-generated method stub
						System.out.println("############# new message received : ");
					
						String chNm = (String) args[0];
						String name = (String) args[1];
						JSONObject dt = (JSONObject) args[2];
						System.out.println(chNm+" : "+name+" : "+dt);
					}
				});
				
				System.out.println("===== create channel callback");
				xpush.send(channelName, "testkey", new JSONObject(), new Emitter.Listener() {
					public void call(Object... arg0) {
						System.out.println("============ send message complete");
					}
				});
				
				xpush2.send(channelName, "testkey2", new JSONObject(), new Emitter.Listener() {
					public void call(Object... arg0) {
						System.out.println("============ send message complete");
					}
				});
				
			}
		});
    	
    	Thread.sleep(10000000);
    }
}
