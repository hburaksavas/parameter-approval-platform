package com.example.parameterapproval.query;

import com.example.parameterapproval.common.NotFoundException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class CustomQueryRegistry {

    private final List<CustomQueryProvider> providers;
    private final Map<String, CustomQueryProvider> byCode = new LinkedHashMap<>();

    public CustomQueryRegistry(List<CustomQueryProvider> providers) {
        this.providers = providers;
    }

    @PostConstruct
    void initialize() {
        providers.forEach(provider -> {
            CustomParameterQuery annotation = provider.getClass().getAnnotation(CustomParameterQuery.class);
            if (annotation == null) {
                throw new IllegalStateException(provider.getClass().getName() + " @CustomParameterQuery içermeli");
            }
            String code = normalize(annotation.code());
            if (!code.equals(normalize(provider.metadata().code()))) {
                throw new IllegalStateException("Custom query annotation/metadata code eşleşmiyor: " + code);
            }
            if (byCode.putIfAbsent(code, provider) != null) {
                throw new IllegalStateException("Tekrarlanan custom query code: " + code);
            }
        });
    }

    public List<CustomQueryMetadata> all() {
        return byCode.values().stream().map(CustomQueryProvider::metadata)
                .sorted(Comparator.comparing(CustomQueryMetadata::title)).toList();
    }

    public CustomQueryProvider provider(String code) {
        CustomQueryProvider provider = byCode.get(normalize(code));
        if (provider == null) throw new NotFoundException("Özel sorgu bulunamadı: " + code);
        return provider;
    }

    private static String normalize(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}

