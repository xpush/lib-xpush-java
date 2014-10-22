package io.stalk.xpush.user;


import java.io.IOException;
import java.net.ConnectException;
import java.util.List;

import io.stalk.xpush.ChannelConnection;
import io.stalk.xpush.XPush;
import io.stalk.xpush.XPushEmitter;
import io.stalk.xpush.exception.AuthorizationFailureException;
import io.stalk.xpush.exception.ChannelConnectionException;
import io.stalk.xpush.model.Channel;
import io.stalk.xpush.model.User;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.github.nkzawa.emitter.Emitter;
import com.google.gson.JsonObject;

public class ChannelTest {
	
	private String host = "http://www.notdol.com:8000";
	private String appId = "stalk-io";

	private String[] users_id = {"USER100","USER101","USER102"};
	private String[] devices_id = {"WEB","WEB","WEB"};
	private String password = "1q2w3e4r";
	
	/*
	@Test 
	public void createChannelAndJoin() throws InterruptedException{
    	final XPush xpush = new XPush(host, appId);
    	final XPush xpush2 = new XPush(host, appId);
		try {
			String returnLogin = xpush.login(users_id[0], password, devices_id[0]);
	    	System.out.println(returnLogin);
	    	Assert.assertEquals(null, returnLogin);
		} catch (AuthorizationFailureException e) {
			System.out.println("인증 오류"+e.getStatus()+"-"+e.getMessage());
		} catch (ChannelConnectionException e) {
			System.out.println("서버 연결 오류 : "+e.getStatus()+"-"+e.getMessage());
		} catch (IOException e) {
			System.out.println("서버 연결 오류 : "+e.getMessage());
		}

		try {
			String returnLogin2 = xpush2.login(users_id[1], password, devices_id[1]);
	    	System.out.println(returnLogin2);
	    	Assert.assertEquals(null, returnLogin2);   
		} catch (AuthorizationFailureException e) {
			System.out.println("인증 오류 : "+e.getStatus()+"-"+e.getMessage());
		} catch (ChannelConnectionException e) {
			System.out.println("서버 연결 오류 : "+e.getStatus()+"-"+e.getMessage());
		} catch (IOException e) {
			System.out.println("서버 연결 오류 : "+e.getMessage());
		}
	    	
	    	xpush.createChannel( new String[]{users_id[2]} , null, new JSONObject(), new XPushEmitter.createChannelListener() {
					public void call(ChannelConnectionException e, final String channelName,
							ChannelConnection ch, List<User> users) {
						
						xpush.getUserListInChannel(channelName, new XPushEmitter.receiveUserList() {
							public void call(String err, List<User> users) {
								System.out.println("********** "+users);
								Assert.assertEquals(users.size(), 2);
								xpush2.joinChannel(channelName, users_id[1], new Emitter.Listener() {
									public void call(Object... args) {
										// TODO Auto-generated method stub
										xpush.getUserListInChannel(channelName, new XPushEmitter.receiveUserList() {
											public void call(String err, List<User> users) {
												System.out.println("********** "+users);
												Assert.assertEquals(users.size(), 3);
											}
										});
									}
								});
							}
							});
						
					}
	    	});
	    	
    	Thread.sleep(5000);
	}	
	 */
	
	/*
	@Test
    public void createChannelWithNoName() throws InterruptedException{
    	final XPush xpush = new XPush(host, appId);
    	String returnLogin = null;
		try {
			returnLogin = xpush.login(users_id[0], password, devices_id[0]);
		} catch (AuthorizationFailureException e) {
			e.printStackTrace();
		} catch (ChannelConnectionException e){
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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

    private void sameChannelNameError(XPush xpush,String channelName){
		try {
			String returnLogin = xpush.login(users_id[0], password, devices_id[0]);
		} catch (AuthorizationFailureException e) {
			System.out.println("인증 실패");
		} catch (ChannelConnectionException e){
			System.out.println("서버 접속 실패");
		} catch (IOException e) {
			System.out.println("서버 접속 실패");
		}
    	xpush.createChannel( new String[]{users_id[1]}, channelName, new JSONObject(), new XPushEmitter.createChannelListener() {
			public void call(ChannelConnectionException e, String channelName,
					ChannelConnection ch, List<User> users) {
				if(e != null){
					System.out.println("****************** "+e.getStatus()+ " : "+e.getMessage());
				}
				Assert.assertEquals("wrong channels", e.getStatus(), "WARN-EXISTED");
				System.out.println("===== create channel callback");
				System.out.println("channel name : "+ channelName);
			}
		});
    }
	*/
	
	/*
	@Test
	public void getChannels() throws InterruptedException{
		final XPush xpush = new XPush(host, appId);
		String returnLogin = null;
		String userId = users_id[0];
		String deviceId = devices_id[0];
		
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
		} catch (ChannelConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println(returnLogin);
    	Assert.assertEquals(null, returnLogin);

    	xpush.getChannels(new XPushEmitter.receiveChannelList() {
			
			@Override
			public void call(String err,
					List<Channel> channels) {
				// TODO Auto-generated method stub
				System.out.println("error : "+err);
				System.out.println("there are "+channels.size()+" channels exist!");
		    	Assert.assertEquals("there is no channels!",true, channels.size() > 0 );
				
				xpush.disconnect();
			}
		});
    	Thread.sleep(5000);
	}
	*/

	/*
	@Test 
	public void createChannelAndExit() throws InterruptedException{
    	final XPush xpush = new XPush(host, appId);
    	final XPush xpush2 = new XPush(host, appId);
		try {
			String returnLogin = xpush.login(users_id[0], password, devices_id[0]);
	    	System.out.println(returnLogin);
	    	Assert.assertEquals(null, returnLogin);
		} catch (AuthorizationFailureException e) {
			System.out.println("인증 오류"+e.getStatus()+"-"+e.getMessage());
		} catch (ChannelConnectionException e) {
			System.out.println("서버 연결 오류 : "+e.getStatus()+"-"+e.getMessage());
		} catch (IOException e) {
			System.out.println("서버 연결 오류 : "+e.getMessage());
		}

		try {
			String returnLogin2 = xpush2.login(users_id[1], password, devices_id[1]);
	    	System.out.println(returnLogin2);
	    	Assert.assertEquals(null, returnLogin2);   
		} catch (AuthorizationFailureException e) {
			System.out.println("인증 오류 : "+e.getStatus()+"-"+e.getMessage());
		} catch (ChannelConnectionException e) {
			System.out.println("서버 연결 오류 : "+e.getStatus()+"-"+e.getMessage());
		} catch (IOException e) {
			System.out.println("서버 연결 오류 : "+e.getMessage());
		}
			
    	xpush.createChannel( users_id, null, new JSONObject(), new XPushEmitter.createChannelListener() {
				public void call(ChannelConnectionException e, final String channelName,
						ChannelConnection ch, List<User> users) {
					
					xpush.getUserListInChannel(channelName, new XPushEmitter.receiveUserList() {
						public void call(String err, List<User> users) {
							System.out.println("********** "+users);
							Assert.assertEquals(users.size(), 3);
							try {
								xpush2.exitChannel(channelName, new Emitter.Listener() {
									public void call(Object... args) {
										// TODO Auto-generated method stub
										xpush.getUserListInChannel(channelName, new XPushEmitter.receiveUserList() {
											public void call(String err, List<User> users) {
												System.out.println("********** "+users);
												Assert.assertEquals(users.size(), 2);
											}
										});
									}
								});
							} catch (ChannelConnectionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						});
					
				}
    	});
	    	
    	Thread.sleep(5000);
	}
	*/
	
    @Test
    public void createChannelWithNoNameAndSend() throws InterruptedException{
    	final XPush xpush = new XPush(host, appId);
    	final XPush xpush2 = new XPush(host, appId);
    	
		try {
			String returnLogin = xpush.login(users_id[0], password, devices_id[0]);
	    	System.out.println(returnLogin);
	    	Assert.assertEquals(null, returnLogin);
		} catch (AuthorizationFailureException e) {
			System.out.println("인증 오류"+e.getStatus()+"-"+e.getMessage());
		} catch (ChannelConnectionException e) {
			System.out.println("서버 연결 오류 : "+e.getStatus()+"-"+e.getMessage());
		} catch (IOException e) {
			System.out.println("서버 연결 오류 : "+e.getMessage());
		}

		try {
			String returnLogin2 = xpush2.login(users_id[1], password, devices_id[1]);
	    	System.out.println(returnLogin2);
	    	Assert.assertEquals(null, returnLogin2);   
		} catch (AuthorizationFailureException e) {
			System.out.println("인증 오류 : "+e.getStatus()+"-"+e.getMessage());
		} catch (ChannelConnectionException e) {
			System.out.println("서버 연결 오류 : "+e.getStatus()+"-"+e.getMessage());
		} catch (IOException e) {
			System.out.println("서버 연결 오류 : "+e.getMessage());
		}
    	
		xpush2.onMessageReceived(new XPushEmitter.messageReceived() {
			@Override
			public void call(String channelName, String key, JSONObject value) {
				System.out.println("############# new message received : ");
				System.out.println(channelName+" : "+key+" : "+value);
			}
		});
    	xpush.createChannel( new String[]{users_id[1],users_id[2]}, null, new JSONObject(), new XPushEmitter.createChannelListener() {
				
				public void call(ChannelConnectionException e, String channelName,
						ChannelConnection ch, List<User> users) {
				// TODO Auto-generated method stub
				
				
				System.out.println("===== create channel callback : "+channelName);
				
				xpush.send(channelName, "message", new JSONObject(), new Emitter.Listener() {
					public void call(Object... arg0) {
						System.out.println("============ send message complete");
					}
				});
				xpush.send(channelName, "message", new JSONObject(), new Emitter.Listener() {
					public void call(Object... arg0) {
						System.out.println("============ send message complete");
					}
				});
				xpush.send(channelName, "message", new JSONObject(), new Emitter.Listener() {
					public void call(Object... arg0) {
						System.out.println("============ send message complete");
					}
				});
				xpush.send(channelName, "message", new JSONObject(), new Emitter.Listener() {
					public void call(Object... arg0) {
						System.out.println("============ send message complete");
					}
				});
				
				xpush2.send(channelName, "message", new JSONObject(), new Emitter.Listener() {
					public void call(Object... arg0) {
						System.out.println("============ send message complete");
					}
				});
				
			}
		});
    	
    	Thread.sleep(5000);
    }
}
