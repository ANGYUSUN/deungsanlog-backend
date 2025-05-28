package com.deungsanlog.mountain.dto;


import com.deungsanlog.mountain.entity.Mountain;
import com.deungsanlog.mountain.entity.MountainDescription;
import com.deungsanlog.mountain.entity.MountainSunInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


//3개 엔티티를 하나로 묶어주는 역할
//Service에서 3개 Repository 조회 결과를 담아서 Controller로 전달
//Lombok으로 getter/setter 자동 생성

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MountainDetailDto {

    // 산 기본 정보
    private Mountain mountain;

    // 산 상세 설명
    private MountainDescription description;

    // 일출/일몰 정보
    private MountainSunInfo sunInfo;
}