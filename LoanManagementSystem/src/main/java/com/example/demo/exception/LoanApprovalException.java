package com.example.demo.exception;

public class LoanApprovalException extends RuntimeException {
	
	 private static final long serialVersionUID = 1L;
	
    public LoanApprovalException(String message) {
        super(message);
    }
}