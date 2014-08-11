package io.stalk.xpush.user;

import io.stalk.xpush.XPush;

import org.junit.Assert;
import org.junit.Test;


public class RegisterUser {
	
	private String host = "http://www.notdol.com:8000";
	private String appId = "stalk-io";

    @Test                                                         
    public void loginAndConnect() throws InterruptedException{
    	XPush xpush = new XPush(host, appId);
    	String returnLogin = xpush.login("notdol110", "win1234", "WEB");
    	System.out.println(returnLogin);
    	Assert.assertEquals(null, returnLogin);   
    	Thread.sleep(5000);
    }

    
    
}
