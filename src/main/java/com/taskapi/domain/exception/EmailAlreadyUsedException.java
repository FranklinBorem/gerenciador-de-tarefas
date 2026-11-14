package com.taskapi.domain.exception;

public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException() {
        super("E-mail já cadastrado");
    }
}
