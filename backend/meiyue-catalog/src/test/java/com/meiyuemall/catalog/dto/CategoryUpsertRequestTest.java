package com.meiyuemall.catalog.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I18：类目写请求校验（名称必填）。
 */
class CategoryUpsertRequestTest {

    private static Validator validator;

    @BeforeAll
    static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void blankNameFails() {
        var req = new CategoryUpsertRequest(null, "  ", 0);
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void validNamePasses() {
        var req = new CategoryUpsertRequest(null, "美妆", 10);
        assertTrue(validator.validate(req).isEmpty());
    }
}
