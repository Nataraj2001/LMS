package com.example.demo.exception;

public class LoanRepaymentFailedException extends RuntimeException{

	 private static final long serialVersionUID = 1L;
	   public LoanRepaymentFailedException(String message) {
	       super(message);
	   }
	}