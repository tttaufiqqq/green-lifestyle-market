package com.glm.cart.dto;

import java.util.List;

public record CartView(
    List<SellerGroupView> groups,
    int totalItems,
    boolean hasWarnings
) {}
