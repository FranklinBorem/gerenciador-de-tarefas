package com.taskapi.adapter.in.web.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskRequest(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 120, message = "O título pode ter no máximo 120 caracteres") String title,

        @Size(max = 1000, message = "A descrição pode ter no máximo 1000 caracteres") String description,

        @FutureOrPresent(message = "O prazo não pode estar no passado") LocalDate dueDate) {
}
