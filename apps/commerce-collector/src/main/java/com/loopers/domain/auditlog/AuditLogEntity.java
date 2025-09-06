package com.loopers.domain.auditlog;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "event_log")
public class AuditLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String eventId;
    private String message;
    private String topic;

    public AuditLogEntity(String eventId, String topic, Object messages) {
        this.eventId = eventId;
        this.topic = topic;
        this.message = messages.toString();
    }
}
