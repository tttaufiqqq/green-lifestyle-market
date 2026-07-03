package com.glm.cart.dto;

import java.util.List;

public record SellerGroupView(
    Long sellerId,
    String sellerName,
    List<CartItemView> items
) {}
