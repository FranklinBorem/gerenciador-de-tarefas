package com.taskapi.adapter.in.web;

import com.taskapi.adapter.in.web.dto.AuthResponse;
import com.taskapi.adapter.in.web.dto.LoginRequest;
import com.taskapi.adapter.in.web.dto.RegisterRequest;
import com.taskapi.application.port.in.LoginUseCase;
import com.taskapi.application.port.in.RegisterUserUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação")
public class AuthController {

    private final RegisterUserUseCase registerUser;
    private final LoginUseCase login;

    public AuthController(RegisterUserUseCase registerUser, LoginUseCase login) {
        this.registerUser = registerUser;
        this.login = login;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        String token = registerUser.register(
                new RegisterUserUseCase.Command(request.name(), request.email(), request.password()));
        return new AuthResponse(token);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return new AuthResponse(login.login(request.email(), request.password()));
    }
}
