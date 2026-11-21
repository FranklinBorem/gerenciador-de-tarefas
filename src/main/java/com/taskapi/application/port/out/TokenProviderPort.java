package com.taskapi.application.port.out;

import java.util.Optional;

public interface TokenProviderPort {

    String generate(String email);

    /** Devolve o e-mail guardado no token, ou vazio se o token for inválido ou estiver vencido. */
    Optional<String> extractEmail(String token);
}
