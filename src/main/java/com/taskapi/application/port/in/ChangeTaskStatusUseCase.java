package com.taskapi.application.port.in;

import com.taskapi.domain.model.Task;
import com.taskapi.domain.model.TaskStatus;

public interface ChangeTaskStatusUseCase {

    Task changeStatus(Long userId, Long taskId, TaskStatus newStatus);
}
