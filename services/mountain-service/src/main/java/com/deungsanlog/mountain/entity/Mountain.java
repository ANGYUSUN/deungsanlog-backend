package com.deungsanlog.mountain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mountains")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Mountain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "elevation")
    private Integer elevation;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "thumbnail_img_url", length = 255)
    private String thumbnailImgUrl;

    @Column(name = "external_id", length = 50)
    private String externalId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}