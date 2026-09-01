package com.example.parameterapproval.sample;

import com.example.parameterapproval.metadata.FilterField;
import com.example.parameterapproval.metadata.FilterInput;
import com.example.parameterapproval.metadata.FilterOperator;
import com.example.parameterapproval.metadata.ParameterField;
import com.example.parameterapproval.metadata.ParameterResource;
import com.example.parameterapproval.metadata.StaticOptions;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "PRM_LOAN_PRODUCT")
@ParameterResource(code = "LOAN_PRODUCT", title = "Kredi Ürünleri", order = 10)
public class LoanProduct {

    @Id
    @Column(name = "PRODUCT_CODE", nullable = false, length = 30)
    @NotBlank
    @Size(max = 30)
    @ParameterField(label = "Ürün Kodu", order = 10, required = true)
    @FilterField(operators = {FilterOperator.EQ, FilterOperator.CONTAINS})
    private String code;

    @Column(name = "PRODUCT_NAME", nullable = false, length = 150)
    @NotBlank
    @Size(max = 150)
    @ParameterField(label = "Ürün Adı", order = 20, required = true)
    @FilterField(operators = {FilterOperator.EQ, FilterOperator.CONTAINS, FilterOperator.STARTS_WITH})
    private String name;

    @Column(name = "CURRENCY_CODE", nullable = false, length = 3)
    @NotBlank
    @Size(min = 3, max = 3)
    @ParameterField(label = "Döviz", order = 30, required = true)
    @FilterField(input = FilterInput.SELECT, operators = FilterOperator.EQ)
    @StaticOptions({"TRY", "USD", "EUR", "GBP"})
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    @NotNull
    @ParameterField(label = "Durum", order = 40, required = true)
    @FilterField(input = FilterInput.SELECT, operators = FilterOperator.EQ)
    private ProductStatus status;

    @Version
    @Column(name = "ROW_VERSION", nullable = false)
    private Long version;

    protected LoanProduct() { }
}
