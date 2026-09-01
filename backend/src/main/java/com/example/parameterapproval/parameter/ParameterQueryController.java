package com.example.parameterapproval.parameter;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/parameter-resources")
public class ParameterQueryController {

    private final ParameterSearchService searchService;

    public ParameterQueryController(ParameterSearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/{resourceCode}/search")
    PageResponse<Map<String, Object>> search(
            @PathVariable String resourceCode,
            @Valid @RequestBody ParameterSearchRequest request) {
        return searchService.search(resourceCode, request);
    }
}

