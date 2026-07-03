package com.glm.cart.dto;

import java.math.BigDecimal;

public record CartItemView(
    Long id,
    Long productId,
    String title,
    String slug,
    String imagePath,
    BigDecimal price,
    BigDecimal priceSnapshot,
    int quantity,
    int available,
    boolean priceChanged,
    boolean outOfStock
) {}
