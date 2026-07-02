package com.glm.common.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Stub audit logger — logs to SLF4J until spec 10 adds the AuditLog entity + repository.
 */
@Component
public class AuditLogger {

    private static final Logger log = LoggerFactory.getLogger(AuditLogger.class);

    public void log(String action, String actorId, String resourceType, String resourceId) {
        log.info("AUDIT action={} actor={} resource={}/{}", action, actorId, resourceType, resourceId);
    }
}
