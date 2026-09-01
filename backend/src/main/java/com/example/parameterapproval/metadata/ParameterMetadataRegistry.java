package com.example.parameterapproval.metadata;

import com.example.parameterapproval.common.BusinessException;
import com.example.parameterapproval.common.NotFoundException;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Version;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class ParameterMetadataRegistry {

    private final EntityManager entityManager;
    private final Map<String, ResourceDescriptor> descriptors = new LinkedHashMap<>();

    public ParameterMetadataRegistry(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @PostConstruct
    void initialize() {
        entityManager.getMetamodel().getEntities().stream()
                .map(EntityType::getJavaType)
                .filter(type -> type.isAnnotationPresent(ParameterResource.class))
                .forEach(this::register);
    }

    public Collection<ResourceMetadata> allMetadata() {
        return descriptors.values().stream()
                .map(ResourceDescriptor::metadata)
                .sorted(Comparator.comparingInt(ResourceMetadata::order).thenComparing(ResourceMetadata::title))
                .toList();
    }

    public ResourceDescriptor descriptor(String resourceCode) {
        ResourceDescriptor descriptor = descriptors.get(normalize(resourceCode));
        if (descriptor == null) {
            throw new NotFoundException("Parametre kaynağı bulunamadı: " + resourceCode);
        }
        return descriptor;
    }

    public ResourceMetadata metadata(String resourceCode) {
        return descriptor(resourceCode).metadata();
    }

    private void register(Class<?> entityClass) {
        ParameterResource resource = entityClass.getAnnotation(ParameterResource.class);
        List<Field> fields = allFields(entityClass);
        Field idField = fields.stream().filter(field -> field.isAnnotationPresent(Id.class)).findFirst()
                .orElseThrow(() -> new IllegalStateException(entityClass.getName() + " için @Id alanı bulunamadı"));
        Field versionField = fields.stream().filter(field -> field.isAnnotationPresent(Version.class)).findFirst().orElse(null);

        Map<String, Field> exposed = new LinkedHashMap<>();
        List<FieldMetadata> fieldMetadata = fields.stream()
                .filter(field -> field.isAnnotationPresent(ParameterField.class))
                .map(field -> {
                    ReflectionUtils.makeAccessible(field);
                    exposed.put(field.getName(), field);
                    return toMetadata(field, idField.equals(field));
                })
                .sorted(Comparator.comparingInt(FieldMetadata::order).thenComparing(FieldMetadata::name))
                .toList();

        if (!exposed.containsKey(idField.getName())) {
            throw new IllegalStateException(entityClass.getName() + " içindeki @Id alanı @ParameterField olmalıdır");
        }
        ReflectionUtils.makeAccessible(idField);
        if (versionField != null) {
            ReflectionUtils.makeAccessible(versionField);
        }

        ResourceMetadata metadata = new ResourceMetadata(
                normalize(resource.code()), resource.title(), resource.order(), idField.getName(), fieldMetadata);
        ResourceDescriptor previous = descriptors.putIfAbsent(metadata.code(),
                new ResourceDescriptor(metadata, entityClass, idField, versionField, exposed));
        if (previous != null) {
            throw new IllegalStateException("Tekrarlanan parameter resource code: " + metadata.code());
        }
    }

    private FieldMetadata toMetadata(Field field, boolean id) {
        ParameterField parameter = field.getAnnotation(ParameterField.class);
        FilterField filter = field.getAnnotation(FilterField.class);
        ReferenceField reference = field.getAnnotation(ReferenceField.class);
        return new FieldMetadata(
                field.getName(), parameter.label(), javaType(field.getType()), parameter.order(), id,
                field.isAnnotationPresent(GeneratedValue.class),
                parameter.editable() && !id, parameter.visible(), parameter.required(), parameter.sensitive(),
                filter != null, filter == null ? null : filter.input(),
                filter == null ? List.of() : List.of(filter.operators()),
                reference == null ? null : new ReferenceMetadata(
                        normalize(reference.resourceCode()), reference.valueField(), reference.labelField()));
    }

    private static List<Field> allFields(Class<?> type) {
        List<Field> result = new ArrayList<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            result.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return result;
    }

    private static String javaType(Class<?> type) {
        if (type == boolean.class || type == Boolean.class) return "BOOLEAN";
        if (Number.class.isAssignableFrom(type) || type.isPrimitive()) return "NUMBER";
        if (Temporal.class.isAssignableFrom(type) || java.util.Date.class.isAssignableFrom(type)) return "DATE";
        if (type.isEnum()) return "ENUM";
        if (type == String.class || type == Character.class || type == char.class) return "STRING";
        return "REFERENCE";
    }

    private static String normalize(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException("Resource code boş olamaz");
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
