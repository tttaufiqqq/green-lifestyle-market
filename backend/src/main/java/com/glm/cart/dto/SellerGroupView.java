package com.glm.cart.dto;

import java.util.List;

public record SellerGroupView(
    Long             sellerId,
    String           sellerName,
    List<CartItemView> items,
    boolean          allowMeetup,   // true if ALL items in group allow meetup
    boolean          allowShipping  // true if ALL items in group allow shipping
) {}
