package com.taskapi.adapter.out.persistence;

import com.taskapi.domain.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataTaskRepository extends JpaRepository<TaskEntity, Long> {

    Page<TaskEntity> findByUserId(Long userId, Pageable pageable);

    Page<TaskEntity> findByUserIdAndStatus(Long userId, TaskStatus status, Pageable pageable);

    Optional<TaskEntity> findByIdAndUserId(Long id, Long userId);
}
