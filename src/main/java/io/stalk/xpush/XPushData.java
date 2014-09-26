package io.stalk.xpush;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class XPushData {
	
	public static final String APP_ID = "A";
	//public static final String APP_ID = "app";
	
	public static final String CHANNEL_ID = "C";
	//public static final String CHANNEL_ID = "channel";
	
	public static final String USER_ID= "U";
	//public static final String USER_ID = "userId";
	
	public static final String USER_IDS = "US";
	//public static final String USER_IDS = "users";
	
	public static final String DEVICE_ID = "D";
	//public static final String DEVICE_ID = "deviceId";

	public static final String NOTI_ID = "N";
	//public static final String NOTI_ID = "notiId";
	
	public static final String SERVER = "S";
	//public static final String SERVER = "server";
	
	public static final String MESSAGE = "MG";
	//public static final String MESSAGE = "message";
	
	public static final String NAME = "NM";
	//public static final String NAME = "name";
	
	public static final String PASSWORD = "PW";
	//public static final String PASSWORD = "password";
	
	public static final String GROUPS = "GR";
	//public static final String GROUPS = "groups";
		
	public static final String DATA = "DT";
	//public static final String DATA = "datas";
	
	public static final String MODE = "MD";
	//public static final String MODE = "mode";

	public static final String TIMESTAMP = "TS";
	//public static final String TIMESTAMP = "timestamp";

	public static final String SOCKET_ID = "SS";
	//public static final String SOCKET_ID = "socketId";

	public static final String CREATE_DATE = "CD";
	//public static final String CREATE_DATE = "createDate";
	
	public static final String UPDATE_DATE = "UD";
	//public static final String UPDATE_DATE = "updateDate";

	public static final String ERROR_INTERNAL = "ERR-INTERNAL";
	
	private static Map<String, String> bindingInfo;
	
	/*
	public static String s(String key){
		if(bindingInfo == null) init();
		return s.get(key);
	}
	
	private static String init(){
		bindingInfo.put(APP_ID, APP_ID_S);
		bindingInfo.put(CHANNEL_ID, CHANNEL_ID_S);
		bindingInfo.put(USER_ID, USER_ID_S);
		bindingInfo.put(USER_IDS, USER_IDS_S);
		bindingInfo.put(DEVICE_ID, DEVICE_ID_S);
		bindingInfo.put(NOTI_ID, NOTI_ID_S);
		bindingInfo.put(SERVER, SERVER_S);
		bindingInfo.put(MESSAGE, MESSAGE_S);
		bindingInfo.put(NAME, NAME_S);
		bindingInfo.put(PASSWORD, PASSWORD_S);
		bindingInfo.put(GROUPS, GROUPS_S);
		bindingInfo.put(DATA, DATA_S);
		bindingInfo.put(MODE, MODE_S);
		bindingInfo.put(TIMESTAMP_S, TIMESTAMP_S);
		bindingInfo.put(SOCKET_ID, SOCKET_ID_S);
		bindingInfo.put(CREATE_DATE, CREATE_DATE_S);
		bindingInfo.put(UPDATE_DATE, UPDATE_DATE_S);
	}
	public static JSONObject convertShortToNormal(JSONObject data, String[] keys){
		
		Object t = null;
		try {
			for(int i = 0 ; i < keys.length; i ++){
				t = data.get(keys[i]);
				data.put(, arg1)
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return data;
	}	
	*/
	
	
	
	
}
