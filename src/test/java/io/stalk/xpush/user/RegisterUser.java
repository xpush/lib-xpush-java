package io.stalk.xpush.user;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;

import io.stalk.xpush.XPush;
import io.stalk.xpush.exception.AuthorizationFailureException;
import io.stalk.xpush.exception.ChannelConnectionException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.nkzawa.emitter.Emitter;
import com.google.gson.JsonObject;

/**
 * 
 * @author xpush
 *
 *	Register users. 
 *  wrong host information.
 *  there is no data in xpush.
 *
 */
public class RegisterUser {
	
	private String host = XPushTestProperites.HOST;
	private String appId = XPushTestProperites.APP_ID;
	
	public static final String[] users_id = {"USER100","USER101","USER102","USER103","USER104"};
	public static final String[] devices_id = {"WEB","WEB","WEB","WEB","WEB"};
	
	private String password = "1q2w3e4r";
	
	private String wrongHost = "http://www.naver.com";
	private String doesNotExistAppId = "honggildong";
	
	@Test
	public void signupAndLogin() throws InterruptedException{
		XPush xpush = new XPush(host, appId);
		XPush xpush2 = new XPush(host, appId);
		XPush xpush3 = new XPush(host, appId);
		XPush xpush4 = new XPush(host, appId);
		XPush xpush5 = new XPush(host, appId);
		try {
			xpush.signup(users_id[0], password, devices_id[0]);
			Assert.assertEquals("SIGN UP SUCCESS [ USER : "+users_id[0]+" -- DEVICE : "+devices_id[0]+"]", null, null);
		} catch (AuthorizationFailureException e) {
			//e.printStackTrace();
			if(AuthorizationFailureException.USER_EXIST.equals(e.getMessage())){
				System.out.println("등록된 사용자 입니다.");
			}
		} catch (ChannelConnectionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			xpush.signup(users_id[1], password, devices_id[1]);
			Assert.assertEquals("SIGN UP SUCCESS [ USER : "+users_id[1]+" -- DEVICE : "+devices_id[1]+"]", null, null);
		} catch (AuthorizationFailureException e) {
			//e.printStackTrace();
			if(AuthorizationFailureException.USER_EXIST.equals(e.getMessage())){
				System.out.println("등록된 사용자 입니다.");
			}
		} catch (ChannelConnectionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			xpush.signup(users_id[2], password, devices_id[2]);
			Assert.assertEquals("SIGN UP SUCCESS [ USER : "+users_id[2]+" -- DEVICE : "+devices_id[2]+"]", null, null);
		} catch (AuthorizationFailureException e) {
			//e.printStackTrace();
			if(AuthorizationFailureException.USER_EXIST.equals(e.getMessage())){
				System.out.println("등록된 사용자 입니다.");
			}
		} catch (ChannelConnectionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		try {
			xpush.signup(users_id[3], password, devices_id[3]);
			Assert.assertEquals("SIGN UP SUCCESS [ USER : "+users_id[3]+" -- DEVICE : "+devices_id[3]+"]", null, null);
		} catch (AuthorizationFailureException e) {
			//e.printStackTrace();
			if(AuthorizationFailureException.USER_EXIST.equals(e.getMessage())){
				System.out.println("등록된 사용자 입니다.");
			}
		} catch (ChannelConnectionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		try {
			xpush.signup(users_id[4], password, devices_id[4]);
			Assert.assertEquals("SIGN UP SUCCESS [ USER : "+users_id[4]+" -- DEVICE : "+devices_id[4]+"]", null, null);
		} catch (AuthorizationFailureException e) {
			//e.printStackTrace();
			if(AuthorizationFailureException.USER_EXIST.equals(e.getMessage())){
				System.out.println("등록된 사용자 입니다.");
			}
		} catch (ChannelConnectionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		try {
			xpush.login(users_id[0], password, devices_id[0]);
			xpush2.login(users_id[1], password, devices_id[1]);
			xpush3.login(users_id[2], password, devices_id[2]);
			xpush4.login(users_id[3], password, devices_id[3]);
			xpush5.login(users_id[4], password, devices_id[4]);
		} catch (AuthorizationFailureException e) {
			System.out.println("인증 오류"+e.getStatus()+"-"+e.getMessage());
		} catch (ChannelConnectionException e) {
			System.out.println("서버 연결 오류 : "+e.getStatus()+"-"+e.getMessage());
		} catch (IOException e) {
			System.out.println("서버 연결 오류 : "+e.getMessage());
		}
		
		Thread.sleep(5000);
	}	
	
    @Test(expected=FileNotFoundException.class)                       
    public void confirmAddress() throws Exception{
    	XPush xpush = new XPush(wrongHost, appId);
    	String returnLogin = null;
    	xpush.login("", "", "");

    	try{
	    	XPush xpush2 = new XPush(host, appId);
	    	String returnLogin2 = null;
			returnLogin2 = xpush2.login(users_id[0], password, devices_id[0]);
		
			returnLogin = xpush.login(users_id[0], password, devices_id[0]);
	    	System.out.println(returnLogin);
	    	Assert.assertEquals(null, returnLogin);   
			
		} catch (AuthorizationFailureException e) {
			System.out.println("인증 오류"+e.getStatus()+"-"+e.getMessage());
		} catch (ChannelConnectionException e) {
			System.out.println("서버 연결 오류 : "+e.getStatus()+"-"+e.getMessage());
		} catch (IOException e) {
			System.out.println("서버 연결 오류 : "+e.getMessage());
		}
    	Thread.sleep(5000);
    }
    
    @Test(expected=AuthorizationFailureException.class)                                     
    public void confirmApplicationId() throws Exception{
    	XPush xpush = new XPush(host, doesNotExistAppId);
    	String returnLogin = null;
		returnLogin = xpush.login(users_id[0], password, devices_id[0]);

    	XPush xpush2 = new XPush(host, appId);
    	String returnLogin2 = null;
		returnLogin2 = xpush2.login(users_id[0], password, devices_id[0]);

    	System.out.println(returnLogin);
    	Assert.assertEquals(null, returnLogin);   
    	Thread.sleep(5000);
    }
	
}
