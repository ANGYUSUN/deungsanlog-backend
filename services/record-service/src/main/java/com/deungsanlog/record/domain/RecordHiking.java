package com.deungsanlog.record.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "record_hikings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RecordHiking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId;

    private Long mountainId;

    private LocalDate recordDate;

    @Column(nullable = false)
    private String photoUrl;

    @Column(length = 100)
    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
