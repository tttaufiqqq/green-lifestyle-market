package com.glm.common.audit;

import com.glm.user.entity.User;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
public class AuditLogger {

    private final AuditLogRepository repo;

    public AuditLogger(AuditLogRepository repo) {
        this.repo = repo;
    }

    @Async
    public void log(User user, String action, String entityType, Long entityId, String ip) {
        AuditLog entry = new AuditLog();
        entry.setUser(user);
        entry.setAction(action);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setIp(ip);
        entry.setCreatedAt(Instant.now());
        repo.save(entry);
    }

    @Async
    public void log(User user, String action, String ip) {
        log(user, action, "USER", user != null ? user.getId() : null, ip);
    }
}
