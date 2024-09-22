package com.distribuidos.authentication.exceptions;

public class CentralizerValidateUserException extends RuntimeException {

    public CentralizerValidateUserException(String documentId) {
        super("Error on centralizer external service when verifying user " + documentId);
    }

}
