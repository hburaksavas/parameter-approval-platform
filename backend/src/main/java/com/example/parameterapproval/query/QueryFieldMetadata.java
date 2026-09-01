package com.example.parameterapproval.query;

import com.example.parameterapproval.metadata.FilterInput;
import com.example.parameterapproval.metadata.FilterOperator;

import java.util.List;

public record QueryFieldMetadata(
        String name,
        String label,
        String dataType,
        FilterInput input,
        List<FilterOperator> operators
) { }

