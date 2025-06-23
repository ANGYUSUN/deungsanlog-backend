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

    private Long meetingId;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private Status status = Status.JOINED;

    private LocalDateTime joinedAt = LocalDateTime.now();

    public enum Status {
        JOINED, CANCELLED
    }
}
