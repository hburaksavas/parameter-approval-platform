package com.example.parameterapproval.parameter;

import com.example.parameterapproval.metadata.FilterOperator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FilterCriterion(@NotBlank String field, @NotNull FilterOperator operator, Object value) { }

