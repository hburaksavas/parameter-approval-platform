package com.example.parameterapproval.parameter;

import com.example.parameterapproval.common.BusinessException;
import com.example.parameterapproval.common.NotFoundException;
import com.example.parameterapproval.metadata.FieldMetadata;
import com.example.parameterapproval.metadata.ParameterMetadataRegistry;
import com.example.parameterapproval.metadata.ResourceDescriptor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class EntityAccessService {

    private final EntityManager entityManager;
    private final ParameterMetadataRegistry registry;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public EntityAccessService(
            EntityManager entityManager,
            ParameterMetadataRegistry registry,
            ObjectMapper objectMapper,
            Validator validator) {
        this.entityManager = entityManager;
        this.registry = registry;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    public Object findRequired(String resourceCode, String recordId) {
        ResourceDescriptor descriptor = registry.descriptor(resourceCode);
        Object id = convert(recordId, descriptor.idField().getType());
        Object entity = entityManager.find(descriptor.entityClass(), id);
        if (entity == null) {
            throw new NotFoundException(resourceCode + " kaydı bulunamadı: " + recordId);
        }
        return entity;
    }

    public ObjectNode snapshot(String resourceCode, Object entity, boolean maskSensitive) {
        ResourceDescriptor descriptor = registry.descriptor(resourceCode);
        ObjectNode result = objectMapper.createObjectNode();
        Map<String, FieldMetadata> metadataByName = descriptor.metadata().fields().stream()
                .collect(java.util.stream.Collectors.toMap(FieldMetadata::name, item -> item));
        descriptor.exposedFields().forEach((name, field) -> {
            Object value = ReflectionUtils.getField(field, entity);
            FieldMetadata metadata = metadataByName.get(name);
            if (maskSensitive && metadata.sensitive() && value != null) {
                result.put(name, "******");
            } else {
                result.set(name, objectMapper.valueToTree(value));
            }
        });
        return result;
    }

    public ObjectNode normalizePayload(String resourceCode, JsonNode payload, OperationMode mode) {
        if (payload == null || !payload.isObject()) {
            throw new BusinessException("newValue JSON object olmalıdır");
        }
        ResourceDescriptor descriptor = registry.descriptor(resourceCode);
        ObjectNode normalized = objectMapper.createObjectNode();
        Iterator<Map.Entry<String, JsonNode>> fields = payload.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            Field field = descriptor.exposedFields().get(entry.getKey());
            if (field == null) {
                throw new BusinessException(resourceCode + " için izin verilmeyen alan: " + entry.getKey());
            }
            boolean idField = field.equals(descriptor.idField());
            FieldMetadata fieldMetadata = descriptor.metadata().fields().stream()
                    .filter(item -> item.name().equals(entry.getKey())).findFirst().orElseThrow();
            if (!idField && !fieldMetadata.editable()) {
                throw new BusinessException("Değiştirilemeyen alan: " + entry.getKey());
            }
            if (mode == OperationMode.UPDATE && idField) {
                continue;
            }
            normalized.set(entry.getKey(), entry.getValue());
        }
        return normalized;
    }

    public Object newEntity(
            String resourceCode, JsonNode payload, Map<String, Object> referencedEntities) {
        ResourceDescriptor descriptor = registry.descriptor(resourceCode);
        Object entity = instantiate(descriptor.entityClass());
        applyPayload(descriptor, entity, normalizePayload(resourceCode, payload, OperationMode.CREATE), true);
        applyReferences(descriptor, entity, referencedEntities);
        validate(entity);
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    public Object updateEntity(
            String resourceCode,
            String recordId,
            JsonNode payload,
            Long expectedVersion,
            Map<String, Object> referencedEntities) {
        ResourceDescriptor descriptor = registry.descriptor(resourceCode);
        Object entity = findRequired(resourceCode, recordId);
        assertVersion(descriptor, entity, expectedVersion);
        applyPayload(descriptor, entity, normalizePayload(resourceCode, payload, OperationMode.UPDATE), false);
        applyReferences(descriptor, entity, referencedEntities);
        validate(entity);
        return entity;
    }

    public Object deleteEntity(String resourceCode, String recordId, Long expectedVersion) {
        ResourceDescriptor descriptor = registry.descriptor(resourceCode);
        Object entity = findRequired(resourceCode, recordId);
        assertVersion(descriptor, entity, expectedVersion);
        entityManager.remove(entity);
        return entity;
    }

    public Long version(String resourceCode, Object entity) {
        Field versionField = registry.descriptor(resourceCode).versionField();
        if (versionField == null) return null;
        Object value = ReflectionUtils.getField(versionField, entity);
        return value == null ? null : ((Number) value).longValue();
    }

    public Object id(String resourceCode, Object entity) {
        return ReflectionUtils.getField(registry.descriptor(resourceCode).idField(), entity);
    }

    public Map<String, Object> resolveReferenceBindings(
            Map<String, String> bindings, Map<String, Object> entitiesByClientReference) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (bindings == null) return result;
        bindings.forEach((field, clientReference) -> {
            Object reference = entitiesByClientReference.get(clientReference);
            if (reference == null) {
                throw new BusinessException("Çözümlenemeyen clientReference: " + clientReference);
            }
            result.put(field, reference);
        });
        return result;
    }

    private void applyReferences(ResourceDescriptor descriptor, Object target, Map<String, Object> references) {
        references.forEach((targetFieldName, referencedEntity) -> {
            Field targetField = descriptor.exposedFields().get(targetFieldName);
            if (targetField == null) {
                throw new BusinessException("Reference binding alanı izinli değil: " + targetFieldName);
            }
            var referenceAnnotation = targetField.getAnnotation(com.example.parameterapproval.metadata.ReferenceField.class);
            if (referenceAnnotation == null) {
                throw new BusinessException(targetFieldName + " alanı @ReferenceField değil");
            }
            ResourceDescriptor referencedDescriptor = registry.descriptor(referenceAnnotation.resourceCode());
            if (!referencedDescriptor.entityClass().isInstance(referencedEntity)) {
                throw new BusinessException(targetFieldName + " için reference tipi uyumsuz");
            }
            Object referencedId = ReflectionUtils.getField(referencedDescriptor.idField(), referencedEntity);
            ReflectionUtils.setField(targetField, target, convert(referencedId, targetField.getType()));
        });
    }

    private void applyPayload(ResourceDescriptor descriptor, Object entity, JsonNode payload, boolean includeId) {
        payload.fields().forEachRemaining(entry -> {
            Field field = descriptor.exposedFields().get(entry.getKey());
            if (!includeId && field.equals(descriptor.idField())) return;
            Object value = entry.getValue().isNull() ? null : objectMapper.convertValue(entry.getValue(), field.getType());
            ReflectionUtils.setField(field, entity, value);
        });
    }

    private void assertVersion(ResourceDescriptor descriptor, Object entity, Long expectedVersion) {
        if (descriptor.versionField() == null) return;
        Long actual = ((Number) Objects.requireNonNull(
                ReflectionUtils.getField(descriptor.versionField(), entity), "Entity version null")).longValue();
        if (expectedVersion == null || !actual.equals(expectedVersion)) {
            throw new ChangeConflictException(
                    "Kayıt talep oluşturulduktan sonra değişmiş. Beklenen version=" + expectedVersion + ", güncel=" + actual);
        }
    }

    private void validate(Object entity) {
        Set<ConstraintViolation<Object>> violations = validator.validate(entity);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(item -> item.getPropertyPath() + ": " + item.getMessage())
                    .sorted()
                    .collect(java.util.stream.Collectors.joining(", "));
            throw new BusinessException("Entity validasyonu başarısız: " + message);
        }
    }

    private Object instantiate(Class<?> type) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.trySetAccessible();
            return constructor.newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(type.getName() + " için parametresiz constructor gerekli", ex);
        }
    }

    public Object convert(Object value, Class<?> targetType) {
        if (value == null || targetType.isInstance(value)) return value;
        return objectMapper.convertValue(value, targetType);
    }

    public enum OperationMode { CREATE, UPDATE }
}

