package io.stalk.xpush.user;

import io.stalk.xpush.XPush;

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

    
    
}
