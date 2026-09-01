package com.example.parameterapproval.metadata;

import java.util.List;

public record ResourceMetadata(
        String code,
        String title,
        int order,
        String idField,
        List<FieldMetadata> fields
) { }

