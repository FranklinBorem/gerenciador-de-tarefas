package com.taskapi.application.port.in;

import com.taskapi.domain.model.User;

import java.util.Optional;

public interface AuthenticateUserUseCase {

    /** Descobre quem é o usuário a partir do token. Vazio se o token for inválido. */
    Optional<User> authenticate(String token);
}
