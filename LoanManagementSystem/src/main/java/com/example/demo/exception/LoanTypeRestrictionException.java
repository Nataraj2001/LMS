package com.example.demo.exception;

public class LoanTypeRestrictionException extends RuntimeException {
	 private static final long serialVersionUID = 1L;
		
	    public LoanTypeRestrictionException(String message) {
	        super(message);
	    }
	}