package com.example.parameterapproval.metadata;

import com.example.parameterapproval.sample.LoanProduct;
import com.example.parameterapproval.sample.LoanRate;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ParameterAnnotationTest {

    @Test
    void sampleEntitiesShouldExposeOnlyAnnotatedFields() {
        ParameterResource product = LoanProduct.class.getAnnotation(ParameterResource.class);
        assertThat(product.code()).isEqualTo("LOAN_PRODUCT");

        Field productCode = Arrays.stream(LoanRate.class.getDeclaredFields())
                .filter(field -> field.getName().equals("productCode"))
                .findFirst().orElseThrow();
        assertThat(productCode.isAnnotationPresent(ParameterField.class)).isTrue();
        assertThat(productCode.getAnnotation(ReferenceField.class).resourceCode()).isEqualTo("LOAN_PRODUCT");
    }
}

