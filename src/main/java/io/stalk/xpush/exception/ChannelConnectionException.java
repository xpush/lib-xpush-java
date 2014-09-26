package io.stalk.xpush.exception;

public class ChannelConnectionException extends Exception {
	
	public static final String STATUS_CHANNEL_EXIST = "WARN-EXISTED";
	
	private String status;
	
    public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public ChannelConnectionException() {
        super();
    }

    public ChannelConnectionException(String status , String msg) {
        super(msg);
        this.status = status;
    }

    public ChannelConnectionException(Exception cause) {
        super(cause);
    }

    public ChannelConnectionException(String status, String msg, Exception cause) {
        super(msg, cause);
        this.status = status;
    }
}
