package org.baderlab.csplugins.enrichmentmap.view.util;

import javax.swing.JDialog;

public interface CardDialogCallback {
	
	public static enum Message {
		INFO, WARN, ERROR
	}
	
	void setMessage(Message severity, String message);
	
	default void clearMessage() {
		setMessage(Message.INFO, "");
	}
	
	default void setMessage(String message) {
		setMessage(Message.INFO, message);
	}
	
	void setFinishButtonEnabled(boolean enabled);

	JDialog getDialogFrame();
	
	void close();
}
