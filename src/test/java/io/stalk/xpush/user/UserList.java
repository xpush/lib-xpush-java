package io.stalk.xpush.user;

import org.json.JSONObject;
import org.junit.Test;

import com.github.nkzawa.emitter.Emitter;

import io.stalk.xpush.XPush;
import io.stalk.xpush.exception.AuthorizationFailureException;
import io.stalk.xpush.exception.ChannelConnectionException;

public class UserList {
	private String host = "http://stalk-front-s01.cloudapp.net:8000";
	private String appId = "test-app";

	@Test
	public void getUserAllList(){
		XPush xpush = new XPush(host, appId);
    	String returnLogin = null;
		try {
			returnLogin = xpush.login("notdol3001", "win1234", "LG-F320L-0168B1456111AB4C");
		} catch (AuthorizationFailureException e) {
			e.printStackTrace();
		} catch (ChannelConnectionException e){
			e.printStackTrace();
		}
		System.out.println(returnLogin);
		
		xpush.getUserList(new JSONObject(), new Emitter.Listener() {
			
			public void call(Object... args) {
				System.out.println("============= finish");
				System.out.println(args[0]);
			}
		});
    	try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
