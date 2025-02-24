package com.example.demo.exception;

public class LoanRejectionException extends RuntimeException{

	 private static final long serialVersionUID = 1L;
		
	    public LoanRejectionException(String message) {
	        super(message);
	    }
	}
