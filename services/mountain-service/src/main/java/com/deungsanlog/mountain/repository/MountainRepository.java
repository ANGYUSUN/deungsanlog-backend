package com.deungsanlog.mountain.repository;



import com.deungsanlog.mountain.entity.Mountain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

//<Mountain entity, primary key type>
//데이터 접근 계층이란 걸 알려주는 어노테이션,spring이 자동으로 이 인터페이스를 빈으로 등록

@Repository
public interface MountainRepository extends JpaRepository<Mountain, Long> {
    List<Mountain> findByName(String name);
    // 구현체 작성 안 함!
}
