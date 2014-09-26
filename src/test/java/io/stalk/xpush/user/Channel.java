package io.stalk.xpush.user;


import java.util.List;

import io.stalk.xpush.XPush;
import io.stalk.xpush.XPushEmitter;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.github.nkzawa.emitter.Emitter;
import com.google.gson.JsonObject;


public class Channel {
	
	private String host = "http://www.notdol.com:8000";
	private String appId = "stalk-io";
	
	
	@Test
	public void getChannels() throws InterruptedException{
		final XPush xpush = new XPush(host, appId);
		String returnLogin = xpush.login("notdol3001", "win1234", "LG-F320L-0168B1456111AB4C");
    	System.out.println(returnLogin);
    	Assert.assertEquals(null, returnLogin);

    	/*
    	xpush.getChannels(new Emitter.Listener() {
			
			public void call(Object... args) {
				// TODO Auto-generated method stub
				System.out.println("=================== received channel list");
				System.out.println(args[1]);
			}
		});
    	*/
    	
    	xpush.getChannels(new XPushEmitter.receiveChannelList() {
			public void call(String err,
					List<io.stalk.xpush.model.Channel> channels) {
				// TODO Auto-generated method stub
				System.out.println("=================== received channel list");
				System.out.println(channels);
				
			}
		});
    	
    	
    	Thread.sleep(5000);
    	
	}
	
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
    @Test
    public void createChannelWithNoName() throws InterruptedException{
    	final XPush xpush = new XPush(host, appId);
    	String returnLogin = xpush.login("notdol101", "win1234", "WEB");
    	System.out.println(returnLogin);
    	Assert.assertEquals(null, returnLogin);   
    	xpush.createChannel( new String[]{"notdol102"}, null, new JsonObject(), new Emitter.Listener() {
			
			public void call(Object... arg0) {
				// TODO Auto-generated method stub
				String result = (String)arg0[0];
				String chName = (String)arg0[1];
				io.stalk.xpush.Channel ch = (io.stalk.xpush.Channel)arg0[2];
				
				System.out.println("===== create channel callback");
				xpush.send(chName, "testkey", new JSONObject(), new Emitter.Listener() {
					
					public void call(Object... arg0) {
						// TODO Auto-generated method stub
						System.out.println("============ send message complete");
					}
				});
			}
		});
    	
    	Thread.sleep(100000);
    }
    
    @Test
    public void createChannelWithNoNameAndSend() throws InterruptedException{
    	final XPush xpush = new XPush(host, appId);
    	String returnLogin = xpush.login("notdol101", "win1234", "WEB");
    	System.out.println(returnLogin);
    	Assert.assertEquals(null, returnLogin);   
    	
    	final XPush xpush2 = new XPush(host, appId);
    	String returnLogin2 = xpush2.login("notdol102", "win1234", "WEB");
    	System.out.println(returnLogin2);
    	Assert.assertEquals(null, returnLogin2);   
    	
    	xpush.createChannel( new String[]{"notdol102"}, null, new JSONObject(), new Emitter.Listener() {
			
			public void call(Object... arg0) {
				// TODO Auto-generated method stub
				String result = (String)arg0[0];
				String chName = (String)arg0[1];
				io.stalk.xpush.Channel ch = (io.stalk.xpush.Channel)arg0[2];
				
				xpush2.on( io.stalk.xpush.Channel.RECEIVE_KEY ,new Emitter.Listener() {
					
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
				xpush.send(chName, "testkey", new JSONObject(), new Emitter.Listener() {
					
					public void call(Object... arg0) {
						// TODO Auto-generated method stub
						System.out.println("============ send message complete");
					}
				});
				
				xpush2.send(chName, "testkey2", new JSONObject(), new Emitter.Listener() {
					
					public void call(Object... arg0) {
						// TODO Auto-generated method stub
						System.out.println("============ send message complete");
					}
				});
				
			}
		});
    	
    	Thread.sleep(10000000);
    }
*/
}
