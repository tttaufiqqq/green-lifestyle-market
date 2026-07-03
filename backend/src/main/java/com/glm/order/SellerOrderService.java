package com.glm.order;

import com.glm.common.error.DomainException;
import com.glm.common.error.ErrorCode;
import com.glm.order.dto.CancelRequest;
import com.glm.order.dto.MeetupRequest;
import com.glm.order.dto.OrderDetailView;
import com.glm.order.dto.OrderSummary;
import com.glm.order.dto.ShipRequest;
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
public class SellerOrderService {

    private static final Map<String, Collection<Status>> TAB_FILTERS = Map.of(
        "needs-action", Set.of(Status.PAID),
        "confirmed",    Set.of(Status.CONFIRMED),
        "in-progress",  Set.of(Status.SHIPPED, Status.READY_FOR_MEETUP),
        "completed",    Set.of(Status.COMPLETED),
        "cancelled",    Set.of(Status.CANCELLED, Status.EXPIRED, Status.REFUND_REQUESTED, Status.REFUNDED)
    );

    @Value("${app.auto-complete-days:7}")
    private int autoCompleteDays;

    private final OrderRepository orderRepo;
    private final OrderTransitionService transitionService;
    private final OrderMapper mapper;

    public SellerOrderService(OrderRepository orderRepo, OrderTransitionService transitionService,
                               OrderMapper mapper) {
        this.orderRepo         = orderRepo;
        this.transitionService = transitionService;
        this.mapper            = mapper;
    }

    @Transactional(readOnly = true)
    public List<OrderSummary> getSales(User seller, String tab) {
        List<Order> orders;
        if (tab == null || "all".equals(tab)) {
            orders = orderRepo.findBySellerIdOrderByCreatedAtDesc(seller.getId());
        } else {
            Collection<Status> statuses = TAB_FILTERS.get(tab);
            if (statuses == null) statuses = Set.of();
            orders = orderRepo.findBySellerIdAndStatusInOrderByCreatedAtDesc(seller.getId(), statuses);
        }
        return orders.stream().map(o -> mapper.toSummary(o, false)).toList();
    }

    @Transactional(readOnly = true)
    public OrderDetailView getSale(User seller, String orderNo) {
        Order order = requireOrder(orderNo);
        requireSellerOwns(seller, order);
        return mapper.toDetail(order, false, autoCompleteDays);
    }

    @Transactional
    public void confirm(User seller, String orderNo) {
        Order order = requireOrder(orderNo);
        requireSellerOwns(seller, order);
        transitionService.confirm(order);
    }

    @Transactional
    public void reject(User seller, String orderNo, CancelRequest req) {
        Order order = requireOrder(orderNo);
        requireSellerOwns(seller, order);
        transitionService.reject(order, seller, req != null ? req.reason() : null);
    }

    @Transactional
    public void ship(User seller, String orderNo, ShipRequest req) {
        Order order = requireOrder(orderNo);
        requireSellerOwns(seller, order);
        transitionService.ship(order, req);
    }

    @Transactional
    public void readyMeetup(User seller, String orderNo, MeetupRequest req) {
        Order order = requireOrder(orderNo);
        requireSellerOwns(seller, order);
        transitionService.readyMeetup(order, req);
    }

    private Order requireOrder(String orderNo) {
        return orderRepo.findByOrderNo(orderNo).orElseThrow(() ->
            new DomainException(ErrorCode.E_NOTFOUND, "Order not found: " + orderNo, 404));
    }

    private void requireSellerOwns(User seller, Order order) {
        if (!order.getSeller().getId().equals(seller.getId())) {
            throw new DomainException(ErrorCode.E_AUTH_OWN, "Not your sale", 403);
        }
    }
}
