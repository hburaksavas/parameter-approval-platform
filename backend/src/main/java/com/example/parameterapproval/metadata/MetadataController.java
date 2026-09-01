package com.example.parameterapproval.metadata;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/parameter-resources")
public class MetadataController {

    private final ParameterMetadataRegistry registry;
    private final FieldOptionsService fieldOptionsService;

    public MetadataController(ParameterMetadataRegistry registry, FieldOptionsService fieldOptionsService) {
        this.registry = registry;
        this.fieldOptionsService = fieldOptionsService;
    }

    @GetMapping
    Collection<ResourceMetadata> resources() {
        return registry.allMetadata();
    }

    @GetMapping("/{resourceCode}/metadata")
    ResourceMetadata metadata(@PathVariable String resourceCode) {
        return registry.metadata(resourceCode);
    }

    @GetMapping("/{resourceCode}/fields/{fieldName}/options")
    List<OptionMetadata> options(@PathVariable String resourceCode, @PathVariable String fieldName) {
        return fieldOptionsService.options(resourceCode, fieldName);
    }
}
