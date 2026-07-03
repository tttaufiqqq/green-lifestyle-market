package com.glm.catalog.query;

import com.glm.catalog.query.dto.ProductDetail;
import com.glm.catalog.query.dto.ProductSummary;
import com.glm.catalog.query.dto.SearchParams;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
public class ProductPublicController {

    private final CatalogQueryService catalogService;

    public ProductPublicController(CatalogQueryService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public Page<ProductSummary> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String fulfilment,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return catalogService.search(
                new SearchParams(q, categoryId, condition, minPrice, maxPrice, fulfilment, sort, page, size));
    }

    @GetMapping("/{slug}")
    public ProductDetail detail(@PathVariable String slug) {
        return catalogService.getProductBySlug(slug);
    }
}
