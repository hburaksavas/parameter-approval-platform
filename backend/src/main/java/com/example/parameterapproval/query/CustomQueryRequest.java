package com.example.parameterapproval.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.Map;

public record CustomQueryRequest(
        Map<String, Object> filters,
        @Min(0) Integer page,
        @Min(1) @Max(200) Integer size
) {
    public Map<String, Object> filtersOrEmpty() { return filters == null ? Map.of() : filters; }
    public int pageOrDefault() { return page == null ? 0 : page; }
    public int sizeOrDefault() { return size == null ? 20 : size; }
}

