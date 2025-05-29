package com.deungsanlog.mountain.repository;

import com.deungsanlog.mountain.entity.MountainDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MountainDescriptionRepository extends JpaRepository<MountainDescription, Long> {
//findById Jpa에서 제공하기 때문에 선언 안해도 됨.
}