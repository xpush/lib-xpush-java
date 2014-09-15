package io.stalk.xpush.user;

import io.stalk.xpush.XPush;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.github.nkzawa.emitter.Emitter;
import com.google.gson.JsonObject;


public class RegisterUser {
	
	private String host = "http://www.notdol.com:8000";
	private String appId = "stalk-io";

	@Test
	public void signupAndLogin(){
		XPush xpush = new XPush(host, appId);
		JSONObject returnSignup = xpush.signup("notdol3000", "win1234", "WEB","NOTIID");
		System.out.println(returnSignup);
	}
	/*
    @Test                                                         
    public void loginAndConnect() throws InterruptedException{
    	XPush xpush = new XPush(host, appId);
    	String returnLogin = xpush.login("notdol101", "win1234", "WEB");
    	System.out.println(returnLogin);
    	Assert.assertEquals(null, returnLogin);   
    	Thread.sleep(5000);
    }

    @Test
    public void sendMessage() throws InterruptedException{
    	System.out.println("==== sendMessage");
    	XPush xpush = new XPush(host, appId);
    	
    	String returnLogin = xpush.login("notdol102", "win1234", "WEB");
    	System.out.println(returnLogin);
    	Assert.assertEquals(null, returnLogin);   
    	
    	JSONObject sendObject = new JSONObject();
    	try {
			sendObject.put("key", "value");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	xpush.send("TEST_CH01", "TESTKEY", sendObject, new Emitter.Listener() {
			
			public void call(Object... arg0) {
				// TODO Auto-generated method stub
				
			}
		});
    	Assert.assertEquals(null, returnLogin);   
    	Thread.sleep(5000);
    }
    */
    
    
}
