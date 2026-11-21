package com.taskapi.application.port.in;

import com.taskapi.domain.model.Task;

public interface GetTaskUseCase {

    Task get(Long userId, Long taskId);
}
