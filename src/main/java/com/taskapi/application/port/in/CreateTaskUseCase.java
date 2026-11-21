package com.taskapi.application.port.in;

import com.taskapi.domain.model.Task;

import java.time.LocalDate;

public interface CreateTaskUseCase {

    Task create(Long userId, Command command);

    record Command(String title, String description, LocalDate dueDate) {
    }
}
