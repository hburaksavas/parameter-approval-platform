package com.example.parameterapproval.query;

import com.example.parameterapproval.parameter.PageResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/custom-queries")
public class CustomQueryController {

    private final CustomQueryRegistry registry;

    public CustomQueryController(CustomQueryRegistry registry) {
        this.registry = registry;
    }

    @GetMapping
    List<CustomQueryMetadata> all() {
        return registry.all();
    }

    @GetMapping("/{code}/metadata")
    CustomQueryMetadata metadata(@PathVariable String code) {
        return registry.provider(code).metadata();
    }

    @PostMapping("/{code}/search")
    PageResponse<Map<String, Object>> search(
            @PathVariable String code, @Valid @RequestBody CustomQueryRequest request) {
        return registry.provider(code).execute(request);
    }
}

