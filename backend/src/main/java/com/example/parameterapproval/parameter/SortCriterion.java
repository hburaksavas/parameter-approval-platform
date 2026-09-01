package com.example.parameterapproval.parameter;

import jakarta.validation.constraints.NotBlank;

public record SortCriterion(@NotBlank String field, Direction direction) {
    public enum Direction { ASC, DESC }
}

