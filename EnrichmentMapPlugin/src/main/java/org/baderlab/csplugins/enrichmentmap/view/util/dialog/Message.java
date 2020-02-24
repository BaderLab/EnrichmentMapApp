package org.baderlab.csplugins.enrichmentmap.view.util.dialog;

import java.util.Objects;

public class Message {

	public static enum MessageType {
		WARN, ERROR
	}
	
	private final MessageType type;
	private final String message;
	
	
	public Message(MessageType type, String message) {
		this.type = Objects.requireNonNull(type);
		this.message = Objects.requireNonNull(message);
	}
	
	public static Message warn(String message) {
		return new Message(MessageType.WARN, message);
	}
	
	public static Message error(String message) {
		return new Message(MessageType.ERROR, message);
	}

	public MessageType getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}

	public boolean isError() {
		return type == MessageType.ERROR;
	}
}
