package com.taskapi.config;

import com.taskapi.application.port.out.PasswordHasherPort;
import com.taskapi.application.port.out.TaskRepositoryPort;
import com.taskapi.application.port.out.TokenProviderPort;
import com.taskapi.application.port.out.UserRepositoryPort;
import com.taskapi.application.service.AuthService;
import com.taskapi.application.service.TaskService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Único lugar que liga o núcleo aos adaptadores.
 * Os serviços do núcleo não têm anotações do Spring: quem os cria é esta classe.
 */
@Configuration
public class BeanConfiguration {

    @Bean
    TaskService taskService(TaskRepositoryPort tasks) {
        return new TaskService(tasks);
    }

    @Bean
    AuthService authService(UserRepositoryPort users, PasswordHasherPort hasher, TokenProviderPort tokens) {
        return new AuthService(users, hasher, tokens);
    }
}
