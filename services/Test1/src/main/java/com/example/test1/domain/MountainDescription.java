package com.example.test1.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "mountain_descriptions")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MountainDescription {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "mountain_id", nullable = false)
    private Mountain mountain;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "full_description", columnDefinition = "TEXT")
    private String fullDescription;

    @Column(name = "sunrise_time")
    private LocalTime sunriseTime;

    @Column(name = "sunset_time")
    private LocalTime sunsetTime;

    @Column(name = "nearby_tour_info", columnDefinition = "TEXT")
    private String nearbyTourInfo;
}
