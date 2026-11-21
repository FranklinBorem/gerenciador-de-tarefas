package com.taskapi.application.port.in;

import com.taskapi.domain.model.Task;

import java.time.LocalDate;

public interface UpdateTaskUseCase {

    Task update(Long userId, Long taskId, Command command);

    record Command(String title, String description, LocalDate dueDate) {
    }
}
