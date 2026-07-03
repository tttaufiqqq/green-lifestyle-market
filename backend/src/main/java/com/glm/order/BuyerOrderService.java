package com.glm.order;

import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.order.dto.CancelRequest;
import com.glm.order.dto.OrderDetailView;
import com.glm.order.dto.OrderSummary;
import com.glm.order.dto.RefundRequestBody;
import com.glm.order.entity.Order;
import com.glm.order.entity.Order.Status;
import com.glm.order.repository.OrderRepository;
import com.glm.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BuyerOrderService {

    private static final Map<String, Collection<Status>> TAB_FILTERS = Map.of(
        "to-pay",      Set.of(Status.PENDING_PAYMENT),
        "to-receive",  Set.of(Status.CONFIRMED, Status.SHIPPED, Status.READY_FOR_MEETUP),
        "completed",   Set.of(Status.COMPLETED),
        "cancelled",   Set.of(Status.CANCELLED, Status.EXPIRED, Status.REFUND_REQUESTED, Status.REFUNDED)
    );

    @Value("${app.auto-complete-days:7}")
    private int autoCompleteDays;

    private final OrderRepository orderRepo;
    private final OrderTransitionService transitionService;
    private final OrderMapper mapper;

    public BuyerOrderService(OrderRepository orderRepo, OrderTransitionService transitionService,
                              OrderMapper mapper) {
        this.orderRepo         = orderRepo;
        this.transitionService = transitionService;
        this.mapper            = mapper;
    }

    @Transactional(readOnly = true)
    public List<OrderSummary> getOrders(User buyer, String tab) {
        List<Order> orders;
        if (tab == null || "all".equals(tab)) {
            orders = orderRepo.findByBuyerIdOrderByCreatedAtDesc(buyer.getId());
        } else {
            Collection<Status> statuses = TAB_FILTERS.get(tab);
            if (statuses == null) statuses = Set.of();
            orders = orderRepo.findByBuyerIdAndStatusInOrderByCreatedAtDesc(buyer.getId(), statuses);
        }
        return orders.stream().map(o -> mapper.toSummary(o, true)).toList();
    }

    @Transactional(readOnly = true)
    public OrderDetailView getOrder(User buyer, String orderNo) {
        Order order = requireOrder(orderNo);
        requireBuyerOwns(buyer, order);
        return mapper.toDetail(order, true, autoCompleteDays);
    }

    @Transactional
    public void cancel(User buyer, String orderNo, CancelRequest req) {
        Order order = requireOrder(orderNo);
        requireBuyerOwns(buyer, order);
        transitionService.buyerCancel(order, buyer, req != null ? req.reason() : null);
    }

    @Transactional
    public void confirmReceipt(User buyer, String orderNo) {
        Order order = requireOrder(orderNo);
        requireBuyerOwns(buyer, order);
        transitionService.confirmReceipt(order);
    }

    @Transactional
    public void requestRefund(User buyer, String orderNo, RefundRequestBody req) {
        Order order = requireOrder(orderNo);
        requireBuyerOwns(buyer, order);
        transitionService.requestRefund(order, buyer, req.reason());
    }

    private Order requireOrder(String orderNo) {
        return orderRepo.findByOrderNo(orderNo).orElseThrow(() ->
            new DomainException(ErrorCode.E_NOTFOUND, "Order not found: " + orderNo, 404));
    }

    private void requireBuyerOwns(User buyer, Order order) {
        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new DomainException(ErrorCode.E_AUTH_OWN, "Not your order", 403);
        }
    }
}
