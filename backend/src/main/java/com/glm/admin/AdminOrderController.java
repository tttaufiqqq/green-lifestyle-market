package com.glm.admin;

import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.order.OrderMapper;
import com.glm.order.dto.OrderDetailView;
import com.glm.order.dto.OrderSummary;
import com.glm.order.entity.Order;
import com.glm.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController {

    @Value("${app.auto-complete-days:7}")
    private int autoCompleteDays;

    private final OrderRepository orderRepo;
    private final OrderMapper     orderMapper;

    public AdminOrderController(OrderRepository orderRepo, OrderMapper orderMapper) {
        this.orderRepo   = orderRepo;
        this.orderMapper = orderMapper;
    }

    @GetMapping
    public List<OrderSummary> list(@RequestParam(required = false) String status) {
        List<Order> orders = status != null
            ? orderRepo.findAllByStatusOrderByCreatedAtDesc(Order.Status.valueOf(status))
            : orderRepo.findAllByOrderByCreatedAtDesc();
        return orders.stream().map(o -> orderMapper.toSummary(o, true)).toList();
    }

    @GetMapping("/{orderNo}")
    public OrderDetailView detail(@PathVariable String orderNo) {
        Order order = orderRepo.findByOrderNo(orderNo).orElseThrow(() ->
            new DomainException(ErrorCode.E_NOTFOUND, "Order not found: " + orderNo, 404));
        // Admin sees full view: seller perspective (includes sellerNet, buyerPhone always)
        return orderMapper.toDetail(order, false, autoCompleteDays);
    }
}
