package com.deungsanlog.record.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "record_badge_stages")
@Getter
public class RecordBadgeStage {

    @Id
    private int stage;

    private String title;

    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
