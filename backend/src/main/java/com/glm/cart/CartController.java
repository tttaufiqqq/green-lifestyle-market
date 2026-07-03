package com.glm.cart;

import com.glm.cart.dto.AddToCartRequest;
import com.glm.cart.dto.CartView;
import com.glm.cart.dto.UpdateQuantityRequest;
import com.glm.common.security.GlmUserDetails;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartView getCart(@AuthenticationPrincipal GlmUserDetails d) {
        return cartService.getCart(d.getUser());
    }

    @PostMapping("/items")
    public CartView addItem(@AuthenticationPrincipal GlmUserDetails d,
                            @Valid @RequestBody AddToCartRequest req) {
        return cartService.addItem(d.getUser(), req.productId(), req.quantity());
    }

    @PatchMapping("/items/{id}")
    public CartView updateQuantity(@AuthenticationPrincipal GlmUserDetails d,
                                   @PathVariable Long id,
                                   @Valid @RequestBody UpdateQuantityRequest req) {
        return cartService.updateQuantity(d.getUser(), id, req.quantity());
    }

    @DeleteMapping("/items/{id}")
    public CartView removeItem(@AuthenticationPrincipal GlmUserDetails d,
                               @PathVariable Long id) {
        return cartService.removeItem(d.getUser(), id);
    }
}
