package com.example.demo.exception;

public class CustomBusinessLogicException extends RuntimeException {
	 private static final long serialVersionUID = 1L;
   public CustomBusinessLogicException(String message) {
   	
       super(message);
   }
}
