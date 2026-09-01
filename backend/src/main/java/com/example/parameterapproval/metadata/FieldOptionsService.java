package com.example.parameterapproval.metadata;

import com.example.parameterapproval.common.BusinessException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@Service
public class FieldOptionsService {

    private final ParameterMetadataRegistry registry;
    private final EntityManager entityManager;

    public FieldOptionsService(ParameterMetadataRegistry registry, EntityManager entityManager) {
        this.registry = registry;
        this.entityManager = entityManager;
    }

    public List<OptionMetadata> options(String resourceCode, String fieldName) {
        ResourceDescriptor descriptor = registry.descriptor(resourceCode);
        Field field = descriptor.exposedFields().get(fieldName);
        if (field == null) throw new BusinessException("Alan bulunamadı: " + fieldName);

        StaticOptions staticOptions = field.getAnnotation(StaticOptions.class);
        if (staticOptions != null) {
            return Arrays.stream(staticOptions.value()).map(value -> new OptionMetadata(value, value)).toList();
        }
        if (field.getType().isEnum()) {
            return Arrays.stream(field.getType().getEnumConstants())
                    .map(value -> new OptionMetadata(value.toString(), value.toString())).toList();
        }
        ReferenceField reference = field.getAnnotation(ReferenceField.class);
        if (reference != null) {
            return referenceOptions(reference);
        }
        return List.of();
    }

    private List<OptionMetadata> referenceOptions(ReferenceField reference) {
        ResourceDescriptor target = registry.descriptor(reference.resourceCode());
        if (!target.exposedFields().containsKey(reference.valueField())
                || !target.exposedFields().containsKey(reference.labelField())) {
            throw new IllegalStateException("Reference value/label alanı metadata içinde değil");
        }
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> criteria = cb.createQuery(Object[].class);
        Root<?> root = criteria.from(target.entityClass());
        criteria.multiselect(root.get(reference.valueField()), root.get(reference.labelField()));
        criteria.orderBy(cb.asc(root.get(reference.labelField())));
        return entityManager.createQuery(criteria).setMaxResults(200).getResultList().stream()
                .map(row -> new OptionMetadata(row[0], String.valueOf(row[1]))).toList();
    }
}

