package com.distribuidos.authentication.exceptions;

public class CitizenCreationException extends RuntimeException{

    public CitizenCreationException(String documentId) {
        super("Error while creating user " + documentId);
    }
}
