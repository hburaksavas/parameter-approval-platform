package com.example.parameterapproval.change;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public record ChangeItemResponse(
        Long id,
        String resourceCode,
        OperationType operation,
        String recordId,
        String clientReference,
        Map<String, String> referenceBindings,
        JsonNode oldValue,
        JsonNode newValue,
        Long expectedVersion,
        Integer executionOrder,
        ChangeItemStatus status
) { }

