package io.stalk.xpush.exception;

public class AuthorizationFailureException extends Exception {
	
	public static final String USER_EXIST = "ERR-USER_EXIST";
	
    public AuthorizationFailureException() {
        super();
    }

    public AuthorizationFailureException(String msg) {
        super(msg);
    }

    public AuthorizationFailureException(Exception cause) {
        super(cause);
    }

    public AuthorizationFailureException(String msg, Exception cause) {
        super(msg, cause);
    }
}
