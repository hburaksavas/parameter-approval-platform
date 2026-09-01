package com.example.parameterapproval.query;

import java.util.List;

public record CustomQueryMetadata(
        String code,
        String title,
        List<QueryFieldMetadata> filters,
        List<QueryColumnMetadata> columns
) { }

