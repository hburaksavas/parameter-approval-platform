package com.example.parameterapproval.parameter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

public record ParameterSearchRequest(
        List<@Valid FilterCriterion> filters,
        @Min(0) Integer page,
        @Min(1) @Max(200) Integer size,
        List<@Valid SortCriterion> sort
) {
    public int pageOrDefault() { return page == null ? 0 : page; }
    public int sizeOrDefault() { return size == null ? 20 : size; }
    public List<FilterCriterion> filtersOrEmpty() { return filters == null ? List.of() : filters; }
    public List<SortCriterion> sortOrEmpty() { return sort == null ? List.of() : sort; }
}

