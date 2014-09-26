package io.stalk.xpush.user;

import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

import com.github.nkzawa.emitter.Emitter;

import io.stalk.xpush.XPush;
import io.stalk.xpush.XPushData;
import io.stalk.xpush.XPushEmitter;
import io.stalk.xpush.exception.AuthorizationFailureException;
import io.stalk.xpush.exception.ChannelConnectionException;
import io.stalk.xpush.model.User;

public class UserList {
	private String host = "http://stalk-front-s01.cloudapp.net:8000";
	private String appId = "test-app";
	
	private String[] users_id = {"USER100","USER101","USER102"};
	private String[] devices_id = {"WEB","WEB","WEB"};
	private String password = "1q2w3e4r";

	@Test
	public void getUserAllList(){
		XPush xpush = new XPush(host, appId);
    	String returnLogin = null;
		try {
			returnLogin = xpush.login(users_id[0], password, devices_id[0]);
		} catch (AuthorizationFailureException e) {
			e.printStackTrace();
		} catch (ChannelConnectionException e){
			e.printStackTrace();
		}
		System.out.println(returnLogin);
		
		xpush.getUserList(new JSONObject(), new XPushEmitter.receiveUserList() {
			
			public void call(String err, List<User> users) {
				// TODO Auto-generated method stub
				System.out.println("*********** received user list");
				System.out.println(err);
				System.out.println(users);
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
