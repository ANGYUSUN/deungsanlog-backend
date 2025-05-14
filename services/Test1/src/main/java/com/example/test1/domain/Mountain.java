package com.example.test1.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mountains")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mountain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String location;

    private Integer elevation;

    private Double latitude;

    private Double longitude;

    @Column(name = "thumbnail_img_url")
    private String thumbnailImgUrl;

    @Column(name = "external_id", unique = true)
    private String externalId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "mountain", cascade = CascadeType.ALL, orphanRemoval = true)
    private MountainDescription description;

    public void setDescription(MountainDescription description) {
        this.description = description;
        if (description != null) {
            description.setMountain(this);
        }
    }
}

