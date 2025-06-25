package com.deungsanlog.meeting.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "meetings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long hostUserId;

    private Long mountainId; // null 허용

    @Column(nullable = false)
    private String mountainName;

    private String title;

    private String description;

    private String location;

    private LocalDate scheduledDate;

    private LocalTime scheduledTime;

    private LocalDate deadlineDate;

    private String gatherLocation;

    private Integer maxParticipants;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingStatus status = MeetingStatus.OPEN;

    private String chatLink;

    @CreationTimestamp // ✅ 생성시간 자동 기록
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // ✅ 수정시간 자동 기록
    private LocalDateTime updatedAt;

}
