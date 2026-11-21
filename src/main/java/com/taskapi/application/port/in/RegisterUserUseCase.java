package com.taskapi.application.port.in;

public interface RegisterUserUseCase {

    /** Cria a conta e devolve o token de acesso. */
    String register(Command command);

    record Command(String name, String email, String password) {
    }
}
