package com.example.parameterapproval.metadata;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

public record ResourceDescriptor(
        ResourceMetadata metadata,
        Class<?> entityClass,
        Field idField,
        Field versionField,
        Map<String, Field> exposedFields
) {
    public ResourceDescriptor {
        exposedFields = new LinkedHashMap<>(exposedFields);
    }
}

