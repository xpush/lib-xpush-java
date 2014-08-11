package io.stalk.xpush;

public interface AsyncCallbackListener {

	public void success(String data);
	public void fail(String error);
}
