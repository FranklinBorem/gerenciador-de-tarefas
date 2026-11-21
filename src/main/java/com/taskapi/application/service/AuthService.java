package com.taskapi.application.service;

import com.taskapi.application.port.in.AuthenticateUserUseCase;
import com.taskapi.application.port.in.LoginUseCase;
import com.taskapi.application.port.in.RegisterUserUseCase;
import com.taskapi.application.port.out.PasswordHasherPort;
import com.taskapi.application.port.out.TokenProviderPort;
import com.taskapi.application.port.out.UserRepositoryPort;
import com.taskapi.domain.exception.EmailAlreadyUsedException;
import com.taskapi.domain.exception.InvalidCredentialsException;
import com.taskapi.domain.model.User;

import java.util.Locale;
import java.util.Optional;

public class AuthService implements RegisterUserUseCase, LoginUseCase, AuthenticateUserUseCase {

    private final UserRepositoryPort users;
    private final PasswordHasherPort hasher;
    private final TokenProviderPort tokens;

    public AuthService(UserRepositoryPort users, PasswordHasherPort hasher, TokenProviderPort tokens) {
        this.users = users;
        this.hasher = hasher;
        this.tokens = tokens;
    }

    @Override
    public String register(RegisterUserUseCase.Command command) {
        String email = normalize(command.email());

        if (users.existsByEmail(email)) {
            throw new EmailAlreadyUsedException();
        }

        User saved = users.save(new User(null, command.name().trim(), email, hasher.hash(command.password())));
        return tokens.generate(saved.getEmail());
    }

    @Override
    public String login(String email, String password) {
        return users.findByEmail(normalize(email))
                .filter(user -> hasher.matches(password, user.getPasswordHash()))
                .map(user -> tokens.generate(user.getEmail()))
                .orElseThrow(InvalidCredentialsException::new);
    }

    @Override
    public Optional<User> authenticate(String token) {
        return tokens.extractEmail(token).flatMap(users::findByEmail);
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
