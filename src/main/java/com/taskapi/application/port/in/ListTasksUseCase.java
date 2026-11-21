package com.taskapi.application.port.in;

import com.taskapi.domain.model.PageResult;
import com.taskapi.domain.model.Task;
import com.taskapi.domain.model.TaskStatus;

public interface ListTasksUseCase {

    PageResult<Task> list(Long userId, TaskStatus status, int page, int size);
}
