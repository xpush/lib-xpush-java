package io.stalk.xpush;

import java.util.List;

import io.stalk.xpush.model.Channel;

import com.google.gson.JsonObject;


public class XPushEmitter {
	
    public static interface receiveChannelList {
    	public abstract void call(String err, List<Channel> channels);
    }
}
