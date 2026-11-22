package com.taskapi.adapter.out.persistence;

import com.taskapi.domain.model.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

/** Representação da tarefa no banco. Só existe aqui no adaptador: o domínio não conhece esta classe. */
@Entity
@Table(name = "tasks")
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    private LocalDate dueDate;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    protected TaskEntity() {
    }

    public TaskEntity(Long id, String title, String description, TaskStatus status,
                      LocalDate dueDate, Instant createdAt, Long userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getUserId() {
        return userId;
    }
}
