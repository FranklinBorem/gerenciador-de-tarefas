package com.taskapi.application.port.in;

public interface DeleteTaskUseCase {

    void delete(Long userId, Long taskId);
}
