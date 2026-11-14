package com.taskapi.domain.model;

import java.util.List;

/** Uma página de resultados, sem depender de nenhum framework. */
public record PageResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
}
