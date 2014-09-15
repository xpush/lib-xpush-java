package io.stalk.xpush.user;

import org.json.JSONObject;
import org.junit.Test;

import com.github.nkzawa.emitter.Emitter;

import io.stalk.xpush.XPush;

public class UserList {
	private String host = "http://www.notdol.com:8000";
	private String appId = "stalk-io";

	@Test
	public void getUserAllList(){
		XPush xpush = new XPush(host, appId);
    	String returnLogin = xpush.login("notdol101", "win1234", "WEB");
		System.out.println(returnLogin);
		
		
		xpush.getUserList(new JSONObject(), new Emitter.Listener() {
			
			public void call(Object... args) {
				// TODO Auto-generated method stub
				System.out.println("============= finish");
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
