package com.distribuidos.authentication.exceptions;

public class BadCredentialsException extends RuntimeException {
    
    public BadCredentialsException(String email) {
        super("Bad credentials for login with email: " + email);
    }
    
}
