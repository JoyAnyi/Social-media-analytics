package com.company.socialanalytics.audit;

import com.company.socialanalytics.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {
    @Column
    private UUID actorUserId;

    @Column(nullable = false, length = 120)
    private String action;

    @Column(nullable = false, length = 500)
    private String details;

    protected AuditLog() {
    }

    public AuditLog(UUID actorUserId, String action, String details) {
        this.actorUserId = actorUserId;
        this.action = action;
        this.details = details;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }
}
