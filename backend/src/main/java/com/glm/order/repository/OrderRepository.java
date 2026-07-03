package com.glm.order.repository;

import com.glm.order.entity.Order;
import com.glm.order.entity.Order.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNo(String orderNo);
    List<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);
    List<Order> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
    List<Order> findByPaymentId(Long paymentId);
    List<Order> findByBuyerIdAndStatus(Long buyerId, Status status);
    List<Order> findBySellerIdAndStatus(Long sellerId, Status status);
}
