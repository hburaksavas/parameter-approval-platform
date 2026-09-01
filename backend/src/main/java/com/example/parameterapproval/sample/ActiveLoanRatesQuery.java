package com.example.parameterapproval.sample;

import com.example.parameterapproval.metadata.FilterInput;
import com.example.parameterapproval.metadata.FilterOperator;
import com.example.parameterapproval.parameter.PageResponse;
import com.example.parameterapproval.query.CustomParameterQuery;
import com.example.parameterapproval.query.CustomQueryMetadata;
import com.example.parameterapproval.query.CustomQueryProvider;
import com.example.parameterapproval.query.CustomQueryRequest;
import com.example.parameterapproval.query.QueryColumnMetadata;
import com.example.parameterapproval.query.QueryFieldMetadata;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@CustomParameterQuery(code = "ACTIVE_LOAN_RATES", title = "Aktif Kredi Ürün Oranları")
public class ActiveLoanRatesQuery implements CustomQueryProvider {

    private final EntityManager entityManager;

    public ActiveLoanRatesQuery(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public CustomQueryMetadata metadata() {
        return new CustomQueryMetadata(
                "ACTIVE_LOAN_RATES",
                "Aktif Kredi Ürün Oranları",
                List.of(
                        new QueryFieldMetadata("productName", "Ürün Adı", "STRING", FilterInput.TEXT,
                                List.of(FilterOperator.CONTAINS)),
                        new QueryFieldMetadata("currency", "Döviz", "STRING", FilterInput.SELECT,
                                List.of(FilterOperator.EQ)),
                        new QueryFieldMetadata("minimumRate", "Minimum Oran", "NUMBER", FilterInput.NUMBER,
                                List.of(FilterOperator.GTE)),
                        new QueryFieldMetadata("effectiveOn", "Geçerlilik Tarihi", "DATE", FilterInput.DATE,
                                List.of(FilterOperator.LTE))
                ),
                List.of(
                        new QueryColumnMetadata("productCode", "Ürün Kodu", "STRING"),
                        new QueryColumnMetadata("productName", "Ürün Adı", "STRING"),
                        new QueryColumnMetadata("currency", "Döviz", "STRING"),
                        new QueryColumnMetadata("termMonth", "Vade", "NUMBER"),
                        new QueryColumnMetadata("interestRate", "Oran", "NUMBER"),
                        new QueryColumnMetadata("effectiveFrom", "Başlangıç", "DATE")
                ));
    }

    @Override
    @SuppressWarnings("unchecked")
    public PageResponse<Map<String, Object>> execute(CustomQueryRequest request) {
        Map<String, Object> filters = request.filtersOrEmpty();
        StringBuilder where = new StringBuilder(" where p.STATUS = 'ACTIVE' and r.PRODUCT_CODE = p.PRODUCT_CODE ");
        Map<String, Object> parameters = new LinkedHashMap<>();

        if (hasText(filters, "productName")) {
            where.append(" and lower(p.PRODUCT_NAME) like :productName ");
            parameters.put("productName", "%" + filters.get("productName").toString().toLowerCase(Locale.ROOT) + "%");
        }
        if (hasText(filters, "currency")) {
            where.append(" and p.CURRENCY_CODE = :currency ");
            parameters.put("currency", filters.get("currency").toString());
        }
        if (hasText(filters, "minimumRate")) {
            where.append(" and r.INTEREST_RATE >= :minimumRate ");
            parameters.put("minimumRate", new BigDecimal(filters.get("minimumRate").toString()));
        }
        if (hasText(filters, "effectiveOn")) {
            where.append(" and r.EFFECTIVE_FROM <= :effectiveOn ");
            parameters.put("effectiveOn", Date.valueOf(LocalDate.parse(filters.get("effectiveOn").toString())));
        }

        String select = "select p.PRODUCT_CODE, p.PRODUCT_NAME, p.CURRENCY_CODE, "
                + "r.TERM_MONTH, r.INTEREST_RATE, r.EFFECTIVE_FROM "
                + "from PRM_LOAN_PRODUCT p, PRM_LOAN_RATE r" + where
                + " order by p.PRODUCT_NAME, r.TERM_MONTH";
        Query dataQuery = entityManager.createNativeQuery(select);
        parameters.forEach(dataQuery::setParameter);
        dataQuery.setFirstResult(request.pageOrDefault() * request.sizeOrDefault());
        dataQuery.setMaxResults(request.sizeOrDefault());
        List<Object[]> rows = dataQuery.getResultList();
        List<Map<String, Object>> content = rows.stream().map(this::toRow).toList();

        Query countQuery = entityManager.createNativeQuery(
                "select count(*) from PRM_LOAN_PRODUCT p, PRM_LOAN_RATE r" + where);
        parameters.forEach(countQuery::setParameter);
        long total = ((Number) countQuery.getSingleResult()).longValue();
        int pages = (int) Math.ceil((double) total / request.sizeOrDefault());
        return new PageResponse<>(content, request.pageOrDefault(), request.sizeOrDefault(), total, pages);
    }

    private Map<String, Object> toRow(Object[] values) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("productCode", values[0]);
        row.put("productName", values[1]);
        row.put("currency", values[2]);
        row.put("termMonth", values[3]);
        row.put("interestRate", values[4]);
        row.put("effectiveFrom", values[5]);
        return row;
    }

    private static boolean hasText(Map<String, Object> values, String key) {
        return values.get(key) != null && !values.get(key).toString().isBlank();
    }
}

