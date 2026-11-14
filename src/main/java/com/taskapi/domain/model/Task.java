package com.taskapi.domain.model;

import com.taskapi.domain.exception.BusinessRuleException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Tarefa do domínio: Java puro, sem anotações de banco nem de framework.
 * As regras de negócio moram aqui dentro.
 */
public class Task {

    private static final int MAX_TITLE_LENGTH = 120;

    private final Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDate dueDate;
    private final Instant createdAt;
    private final Long userId;

    private Task(Long id, String title, String description, TaskStatus status,
                 LocalDate dueDate, Instant createdAt, Long userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.userId = userId;
    }

    /** Cria uma tarefa nova: sempre começa como pendente. */
    public static Task create(String title, String description, LocalDate dueDate, Long userId) {
        validateTitle(title);
        return new Task(null, title.trim(), description, TaskStatus.PENDING, dueDate, Instant.now(), userId);
    }

    /** Reconstrói uma tarefa que já existia (usado ao ler do banco). */
    public static Task restore(Long id, String title, String description, TaskStatus status,
                               LocalDate dueDate, Instant createdAt, Long userId) {
        return new Task(id, title, description, status, dueDate, createdAt, userId);
    }

    public void update(String title, String description, LocalDate dueDate) {
        validateTitle(title);
        this.title = title.trim();
        this.description = description;
        this.dueDate = dueDate;
    }

    public void changeStatus(TaskStatus newStatus) {
        Objects.requireNonNull(newStatus, "O status é obrigatório");

        if (this.status == TaskStatus.DONE && newStatus == TaskStatus.PENDING) {
            throw new BusinessRuleException("Uma tarefa concluída não pode voltar para pendente");
        }
        this.status = newStatus;
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessRuleException("O título é obrigatório");
        }
        if (title.trim().length() > MAX_TITLE_LENGTH) {
            throw new BusinessRuleException("O título pode ter no máximo " + MAX_TITLE_LENGTH + " caracteres");
        }
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
