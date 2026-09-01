package com.example.parameterapproval.change;

import com.example.parameterapproval.metadata.FieldMetadata;
import com.example.parameterapproval.metadata.ParameterMetadataRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ChangeRequestMapper {

    private static final TypeReference<Map<String, String>> STRING_MAP = new TypeReference<>() { };

    private final ObjectMapper objectMapper;
    private final ParameterMetadataRegistry registry;

    public ChangeRequestMapper(ObjectMapper objectMapper, ParameterMetadataRegistry registry) {
        this.objectMapper = objectMapper;
        this.registry = registry;
    }

    public ChangeRequestResponse toResponse(ChangeRequest request, boolean includeItems) {
        List<ChangeItemResponse> items = includeItems
                ? request.getItems().stream().map(this::toResponse).toList()
                : null;
        return new ChangeRequestResponse(
                request.getId(), request.getRequestNo(), request.getTitle(), request.getDescription(),
                request.getStatus(), request.getCreatedBy(), request.getCreatedByName(), request.getCreatedAt(),
                request.getDecidedBy(), request.getDecidedByName(), request.getDecidedAt(),
                request.getDecisionNote(), request.getItems().size(), items);
    }

    private ChangeItemResponse toResponse(ChangeItem item) {
        return new ChangeItemResponse(
                item.getId(), item.getResourceCode(), item.getOperationType(), item.getRecordId(),
                item.getClientReference(), readMap(item.getReferenceBindingsJson()),
                mask(item.getResourceCode(), readTree(item.getOldValueJson())),
                mask(item.getResourceCode(), readTree(item.getNewValueJson())),
                item.getExpectedVersion(), item.getExecutionOrder(), item.getStatus());
    }

    private JsonNode mask(String resourceCode, JsonNode node) {
        if (node == null || !node.isObject()) return node;
        ObjectNode result = ((ObjectNode) node).deepCopy();
        registry.metadata(resourceCode).fields().stream()
                .filter(FieldMetadata::sensitive)
                .map(FieldMetadata::name)
                .filter(result::hasNonNull)
                .forEach(name -> result.put(name, "******"));
        return result;
    }

    private JsonNode readTree(String json) {
        if (json == null) return null;
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Saklanan JSON okunamadı", ex);
        }
    }

    private Map<String, String> readMap(String json) {
        if (json == null) return Map.of();
        try {
            return objectMapper.readValue(json, STRING_MAP);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Reference bindings okunamadı", ex);
        }
    }
}

