package com.glm.notification;

import com.glm.notification.dto.PushSubscriptionRequest;
import com.glm.notification.entity.PushSubscription;
import com.glm.notification.repository.PushSubscriptionRepository;
import com.glm.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PushSubscriptionService {

    private final PushSubscriptionRepository pushSubscriptionRepo;

    public PushSubscriptionService(PushSubscriptionRepository pushSubscriptionRepo) {
        this.pushSubscriptionRepo = pushSubscriptionRepo;
    }

    @Transactional
    public void register(User user, PushSubscriptionRequest req) {
        PushSubscription sub = pushSubscriptionRepo.findByEndpoint(req.endpoint())
            .orElseGet(PushSubscription::new);
        sub.setUser(user);
        sub.setEndpoint(req.endpoint());
        sub.setP256dh(req.keys().p256dh());
        sub.setAuth(req.keys().auth());
        if (sub.getCreatedAt() == null) sub.setCreatedAt(Instant.now());
        pushSubscriptionRepo.save(sub);
    }

    @Transactional
    public void remove(User user, String endpoint) {
        pushSubscriptionRepo.findByEndpoint(endpoint)
            .filter(sub -> sub.getUser().getId().equals(user.getId()))
            .ifPresent(pushSubscriptionRepo::delete);
    }
}
