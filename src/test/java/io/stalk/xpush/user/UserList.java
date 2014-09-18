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
    	String returnLogin = xpush.login("notdol3001", "win1234", "LG-F320L-0168B1456111AB4C");
		System.out.println(returnLogin);
		
		
		xpush.getUserList(new JSONObject(), new Emitter.Listener() {
			
			public void call(Object... args) {
				// TODO Auto-generated method stub
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
