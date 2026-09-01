package com.example.parameterapproval.query;

import com.example.parameterapproval.parameter.PageResponse;

import java.util.Map;

public interface CustomQueryProvider {
    CustomQueryMetadata metadata();
    PageResponse<Map<String, Object>> execute(CustomQueryRequest request);
}

