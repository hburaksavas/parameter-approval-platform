package com.example.parameterapproval.sample;

import com.example.parameterapproval.metadata.FilterField;
import com.example.parameterapproval.metadata.FilterInput;
import com.example.parameterapproval.metadata.FilterOperator;
import com.example.parameterapproval.metadata.ParameterField;
import com.example.parameterapproval.metadata.ParameterResource;
import com.example.parameterapproval.metadata.ReferenceField;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "PRM_LOAN_RATE")
@ParameterResource(code = "LOAN_RATE", title = "Kredi Oranları", order = 20)
public class LoanRate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "prm_loan_rate_seq")
    @SequenceGenerator(name = "prm_loan_rate_seq", sequenceName = "PRM_LOAN_RATE_SEQ", allocationSize = 20)
    @Column(name = "ID")
    @ParameterField(label = "ID", order = 10, editable = false)
    @FilterField(operators = FilterOperator.EQ, input = FilterInput.NUMBER)
    private Long id;

    @Column(name = "PRODUCT_CODE", nullable = false, length = 30)
    @NotNull
    @ParameterField(label = "Ürün", order = 20, required = true)
    @ReferenceField(resourceCode = "LOAN_PRODUCT", valueField = "code", labelField = "name")
    @FilterField(operators = FilterOperator.EQ, input = FilterInput.SELECT)
    private String productCode;

    @Column(name = "TERM_MONTH", nullable = false)
    @NotNull
    @Min(1)
    @Max(120)
    @ParameterField(label = "Vade (Ay)", order = 30, required = true)
    @FilterField(operators = {FilterOperator.EQ, FilterOperator.GTE, FilterOperator.LTE}, input = FilterInput.NUMBER)
    private Integer termMonth;

    @Column(name = "MIN_AMOUNT", nullable = false, precision = 19, scale = 2)
    @NotNull
    @DecimalMin("0.00")
    @ParameterField(label = "Minimum Tutar", order = 40, required = true)
    @FilterField(operators = {FilterOperator.GTE, FilterOperator.LTE}, input = FilterInput.NUMBER)
    private BigDecimal minAmount;

    @Column(name = "INTEREST_RATE", nullable = false, precision = 9, scale = 6)
    @NotNull
    @DecimalMin("0.000001")
    @ParameterField(label = "Faiz Oranı", order = 50, required = true)
    private BigDecimal interestRate;

    @Column(name = "EFFECTIVE_FROM", nullable = false)
    @NotNull
    @ParameterField(label = "Başlangıç Tarihi", order = 60, required = true)
    @FilterField(operators = {FilterOperator.EQ, FilterOperator.GTE, FilterOperator.LTE}, input = FilterInput.DATE)
    private LocalDate effectiveFrom;

    @Version
    @Column(name = "ROW_VERSION", nullable = false)
    private Long version;

    protected LoanRate() { }
}

