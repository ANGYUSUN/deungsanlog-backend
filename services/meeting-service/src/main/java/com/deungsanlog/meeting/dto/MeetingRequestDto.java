package com.deungsanlog.meeting.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class MeetingRequestDto {
    private Long hostUserId;
    private Long mountainId;
    private String title;
    private String description;
    private String location;
    private LocalDate scheduledDate;
    private LocalTime scheduledTime;
    private LocalDate deadlineDate;
    private String gatherLocation;
    private Integer maxParticipants;
    private String chatLink;
}