package com.example.parameterapproval.metadata;

import java.util.List;

public record FieldMetadata(
        String name,
        String label,
        String dataType,
        int order,
        boolean id,
        boolean generated,
        boolean editable,
        boolean visible,
        boolean required,
        boolean sensitive,
        boolean filterable,
        FilterInput filterInput,
        List<FilterOperator> operators,
        ReferenceMetadata reference
) { }
