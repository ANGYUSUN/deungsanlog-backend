package com.deungsanlog.meeting.repository;

import com.deungsanlog.meeting.entity.Meeting;
import com.deungsanlog.meeting.entity.MeetingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    Page<Meeting> findByTitleContainingIgnoreCaseOrMountainNameContainingIgnoreCase(String title, String mountainName, Pageable pageable);

    Page<Meeting> findByStatusAndTitleContainingIgnoreCase(MeetingStatus status, String title, Pageable pageable);

    Page<Meeting> findByStatusAndMountainNameContainingIgnoreCase(MeetingStatus status, String mountainName, Pageable pageable);
}
