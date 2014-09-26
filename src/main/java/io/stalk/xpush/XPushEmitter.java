package io.stalk.xpush;

import java.util.List;

import io.stalk.xpush.exception.ChannelConnectionException;
import io.stalk.xpush.model.Channel;
import io.stalk.xpush.model.User;

import com.google.gson.JsonObject;


public class XPushEmitter {

	
    public static interface createChannelListener {
    	public abstract void call(ChannelConnectionException e , String channelName, ChannelConnection ch, List<User> users);
    }
	
    public static interface receiveChannelList {
    	public abstract void call(String err, List<Channel> channels);
    }
}
