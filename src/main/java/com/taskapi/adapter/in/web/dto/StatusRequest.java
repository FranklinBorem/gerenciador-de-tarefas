package com.taskapi.adapter.in.web.dto;

import com.taskapi.domain.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull(message = "O status é obrigatório") TaskStatus status) {
}
