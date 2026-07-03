package com.glm.catalog;

import com.glm.catalog.dto.ListingResponse;
import com.glm.catalog.dto.StatusPatchRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Page<ListingResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return productService.getAdminProducts(page, size);
    }

    @PatchMapping("/{id}/status")
    public ListingResponse patchStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusPatchRequest req) {
        return productService.adminPatchStatus(id, req.status());
    }
}
