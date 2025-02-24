package com.example.demo.exception;

public class DuplicateLoanApplicationException extends RuntimeException{
	 private static final long serialVersionUID = 1L;
		
	    public DuplicateLoanApplicationException(String message) {
	        super(message);
	    }
	}