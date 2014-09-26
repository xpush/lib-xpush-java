package io.stalk.xpush.exception;

public class AuthorizationFailureException extends Exception {
	
	public static final String USER_EXIST 				= "ERR-USER_EXIST";
	public static final String STATUS_USER_NOT_EXIST 	= "ERR-NOTEXIST";
	public static final String STATUS_ERROR_PASSWORD 	= "ERR-PASSWORD";
	
	private String status;
	
    public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public AuthorizationFailureException() {
        super();
    }

    public AuthorizationFailureException(String status , String msg) {
        super(msg);
        this.status = status;
    }

    public AuthorizationFailureException(Exception cause) {
        super(cause);
    }

    public AuthorizationFailureException(String status, String msg, Exception cause) {
        super(msg, cause);
        this.status = status;
    }
}
