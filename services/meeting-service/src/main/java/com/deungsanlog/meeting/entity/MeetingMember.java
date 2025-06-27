package com.deungsanlog.meeting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "meeting_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"meeting_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meeting_id", nullable = false)
    private Long meetingId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    public enum Status {
        PENDING,     // 신청함 (대기 중)
        ACCEPTED,    // 수락됨 (참가자)
        REJECTED,    // 거절됨
        CANCELLED    // 본인이 취소
    }
}
