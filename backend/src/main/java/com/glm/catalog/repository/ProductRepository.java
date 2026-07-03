package com.glm.catalog.repository;

import com.glm.catalog.entity.Product;
import com.glm.catalog.entity.Product.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    List<Product> findBySellerId(Long sellerId);

    List<Product> findBySellerIdAndStatus(Long sellerId, Status status);

    /**
     * Full-text search via Oracle Text CONTEXT index on title+description.
     * Uses CONTAINS(title, :query, 1) — label 1 corresponds to the index on products(title).
     */
    @Query(value = "SELECT * FROM products WHERE CONTAINS(title, :query, 1) > 0 AND status = 'ACTIVE'",
           nativeQuery = true)
    List<Product> searchByText(@Param("query") String query);
}
