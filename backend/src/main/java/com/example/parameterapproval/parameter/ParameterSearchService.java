package com.example.parameterapproval.parameter;

import com.example.parameterapproval.common.BusinessException;
import com.example.parameterapproval.metadata.FieldMetadata;
import com.example.parameterapproval.metadata.FilterOperator;
import com.example.parameterapproval.metadata.ParameterMetadataRegistry;
import com.example.parameterapproval.metadata.ResourceDescriptor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ParameterSearchService {

    private final EntityManager entityManager;
    private final ParameterMetadataRegistry registry;
    private final EntityAccessService entityAccess;

    public ParameterSearchService(
            EntityManager entityManager,
            ParameterMetadataRegistry registry,
            EntityAccessService entityAccess) {
        this.entityManager = entityManager;
        this.registry = registry;
        this.entityAccess = entityAccess;
    }

    public PageResponse<Map<String, Object>> search(String resourceCode, ParameterSearchRequest request) {
        ResourceDescriptor descriptor = registry.descriptor(resourceCode);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> dataCriteria = cb.createQuery(descriptor.entityClass());
        Root<?> root = dataCriteria.from(descriptor.entityClass());
        List<Predicate> predicates = predicates(cb, root, descriptor, request.filtersOrEmpty());
        dataCriteria.where(predicates.toArray(Predicate[]::new));
        dataCriteria.orderBy(orders(cb, root, descriptor, request.sortOrEmpty()));

        TypedQuery<?> query = entityManager.createQuery(dataCriteria);
        query.setFirstResult(request.pageOrDefault() * request.sizeOrDefault());
        query.setMaxResults(request.sizeOrDefault());
        List<Map<String, Object>> content = query.getResultList().stream()
                .map(entity -> entityAccess.snapshot(resourceCode, entity, true))
                .map(node -> entityAccess.convert(node, Map.class))
                .map(value -> (Map<String, Object>) value)
                .toList();

        CriteriaQuery<Long> countCriteria = cb.createQuery(Long.class);
        Root<?> countRoot = countCriteria.from(descriptor.entityClass());
        countCriteria.select(cb.count(countRoot));
        countCriteria.where(predicates(cb, countRoot, descriptor, request.filtersOrEmpty()).toArray(Predicate[]::new));
        long total = entityManager.createQuery(countCriteria).getSingleResult();
        int pages = (int) Math.ceil((double) total / request.sizeOrDefault());
        return new PageResponse<>(content, request.pageOrDefault(), request.sizeOrDefault(), total, pages);
    }

    private List<Predicate> predicates(
            CriteriaBuilder cb, Root<?> root, ResourceDescriptor descriptor, List<FilterCriterion> filters) {
        Map<String, FieldMetadata> metadata = descriptor.metadata().fields().stream()
                .collect(Collectors.toMap(FieldMetadata::name, Function.identity()));
        List<Predicate> result = new ArrayList<>();
        for (FilterCriterion filter : filters) {
            FieldMetadata field = metadata.get(filter.field());
            if (field == null || !field.filterable()) {
                throw new BusinessException("Filtrelenmesine izin verilmeyen alan: " + filter.field());
            }
            if (!field.operators().contains(filter.operator())) {
                throw new BusinessException(filter.field() + " için desteklenmeyen operatör: " + filter.operator());
            }
            Class<?> javaType = descriptor.exposedFields().get(filter.field()).getType();
            Expression<?> path = root.get(filter.field());
            result.add(predicate(cb, path, javaType, filter));
        }
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate predicate(CriteriaBuilder cb, Expression<?> path, Class<?> type, FilterCriterion filter) {
        FilterOperator operator = filter.operator();
        if (operator == FilterOperator.IS_NULL) return cb.isNull(path);
        if (operator == FilterOperator.IS_NOT_NULL) return cb.isNotNull(path);
        if (operator == FilterOperator.IN) {
            if (!(filter.value() instanceof Collection<?> values)) {
                throw new BusinessException("IN operatörü liste değer bekler");
            }
            return path.in(values.stream().map(value -> entityAccess.convert(value, type)).toList());
        }
        Object value = entityAccess.convert(filter.value(), type);
        return switch (operator) {
            case EQ -> cb.equal(path, value);
            case NE -> cb.notEqual(path, value);
            case CONTAINS -> cb.like(cb.lower((Expression<String>) path), "%" + value.toString().toLowerCase(Locale.ROOT) + "%");
            case STARTS_WITH -> cb.like(cb.lower((Expression<String>) path), value.toString().toLowerCase(Locale.ROOT) + "%");
            case GT -> cb.greaterThan((Expression<? extends Comparable>) path, (Comparable) value);
            case GTE -> cb.greaterThanOrEqualTo((Expression<? extends Comparable>) path, (Comparable) value);
            case LT -> cb.lessThan((Expression<? extends Comparable>) path, (Comparable) value);
            case LTE -> cb.lessThanOrEqualTo((Expression<? extends Comparable>) path, (Comparable) value);
            default -> throw new BusinessException("Desteklenmeyen operatör: " + operator);
        };
    }

    private List<Order> orders(
            CriteriaBuilder cb, Root<?> root, ResourceDescriptor descriptor, List<SortCriterion> sorts) {
        if (sorts.isEmpty()) {
            return List.of(cb.asc(root.get(descriptor.metadata().idField())));
        }
        return sorts.stream().map(sort -> {
            if (!descriptor.exposedFields().containsKey(sort.field())) {
                throw new BusinessException("Sıralanmasına izin verilmeyen alan: " + sort.field());
            }
            return sort.direction() == SortCriterion.Direction.DESC
                    ? cb.desc(root.get(sort.field())) : cb.asc(root.get(sort.field()));
        }).toList();
    }
}

