package com.example.demo.exception;

public class EmailSendingException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
