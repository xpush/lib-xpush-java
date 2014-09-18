package io.stalk.xpush.model;

import java.util.Date;
import java.util.List;

public class Channel {
	private String _id;
	private String channelId;
	private Date createDate;
	private String appId;
	
	private List<User> users;
}
