package com.example.parameterapproval.change;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record ChangeItemCommand(
        @NotBlank String resourceCode,
        @NotNull OperationType operation,
        String recordId,
        String clientReference,
        Map<String, String> referenceBindings,
        JsonNode newValue,
        Integer executionOrder
) { }

