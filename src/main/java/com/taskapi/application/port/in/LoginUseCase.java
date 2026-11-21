package com.taskapi.application.port.in;

public interface LoginUseCase {

    /** Confere e-mail e senha e devolve o token de acesso. */
    String login(String email, String password);
}
