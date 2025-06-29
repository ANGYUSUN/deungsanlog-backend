package com.deungsanlog.meeting.repository;

import com.deungsanlog.meeting.entity.Meeting;
import com.deungsanlog.meeting.entity.MeetingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    Page<Meeting> findByTitleContainingIgnoreCaseOrMountainNameContainingIgnoreCase(String title, String mountainName, Pageable pageable);

    Page<Meeting> findByStatusAndTitleContainingIgnoreCase(MeetingStatus status, String title, Pageable pageable);
    
    List<Meeting> findByHostUserId(Long hostUserId);
    
    List<Meeting> findByScheduledDate(LocalDate scheduledDate);
}
