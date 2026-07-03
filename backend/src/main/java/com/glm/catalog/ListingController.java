package com.glm.catalog;

import com.glm.catalog.dto.ListingRequest;
import com.glm.catalog.dto.ListingResponse;
import com.glm.catalog.dto.StatusPatchRequest;
import com.glm.common.security.GlmUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/me/listings")
public class ListingController {

    private final ProductService productService;

    public ListingController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ListingResponse> myListings(
            @AuthenticationPrincipal GlmUserDetails d,
            @RequestParam(required = false) String status) {
        return productService.getMyListings(d.getUserId(), status);
    }

    @PostMapping
    public ResponseEntity<ListingResponse> create(
            @AuthenticationPrincipal GlmUserDetails d,
            @Valid @RequestBody ListingRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(req, d));
    }

    @PutMapping("/{id}")
    public ListingResponse update(
            @PathVariable Long id,
            @AuthenticationPrincipal GlmUserDetails d,
            @Valid @RequestBody ListingRequest req) {
        return productService.update(id, req, d.getUserId());
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ListingResponse> addImage(
            @PathVariable Long id,
            @AuthenticationPrincipal GlmUserDetails d,
            @RequestParam("image") MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.addImage(id, file, d.getUserId()));
    }

    @DeleteMapping("/{id}/images/{imgId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long id,
            @PathVariable Long imgId,
            @AuthenticationPrincipal GlmUserDetails d) throws IOException {
        productService.deleteImage(id, imgId, d.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ListingResponse patchStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal GlmUserDetails d,
            @Valid @RequestBody StatusPatchRequest req) {
        return productService.patchStatus(id, req.status(), d.getUserId());
    }
}
