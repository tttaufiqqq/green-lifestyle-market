package com.glm.catalog.query.dto;

import java.math.BigDecimal;

public record SearchParams(
        String q,
        Long categoryId,
        String condition,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String fulfilment,
        String sort,
        int page,
        int size
) {}
