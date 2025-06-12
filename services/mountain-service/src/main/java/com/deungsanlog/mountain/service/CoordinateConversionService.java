package com.deungsanlog.mountain.service;


import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * 위도/경도 ↔ 기상청 격자좌표(X,Y) 변환 서비스
 * 기상청 Lambert Conformal Conic Projection 방식 사용
 */
@Service
@Slf4j
public class CoordinateConversionService {

    // 기상청 단기예보 격자 상수
    private static final double RE = 6371.00877;     // 지구반경 (km)
    private static final double GRID = 5.0;          // 격자간격 (km)
    private static final double SLAT1 = 30.0;        // 표준위도1 (degree)
    private static final double SLAT2 = 60.0;        // 표준위도2 (degree)
    private static final double OLON = 126.0;        // 기준점 경도 (degree)
    private static final double OLAT = 38.0;         // 기준점 위도 (degree)
    private static final double XO = 210 / GRID;     // 기준점 X좌표 (grid)
    private static final double YO = 675 / GRID;     // 기준점 Y좌표 (grid)

    private static final double PI = Math.PI;
    private static final double DEGRAD = PI / 180.0;

    // 람베르트 투영법 계산용 변수들 (한 번만 계산)
    private final double re;
    private final double olon;
    private final double olat;
    private final double sn;
    private final double sf;
    private final double ro;

    public CoordinateConversionService() {
        // 람베르트 투영법 파라미터 초기화 (생성자에서 한 번만 계산)
        this.re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        this.olon = OLON * DEGRAD;
        this.olat = OLAT * DEGRAD;

        // 임시 변수 사용해서 계산 후 final 필드에 할당
        double tempSn = Math.tan(PI * 0.25 + slat2 * 0.5) / Math.tan(PI * 0.25 + slat1 * 0.5);
        this.sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(tempSn);

        double tempSf = Math.tan(PI * 0.25 + slat1 * 0.5);
        this.sf = Math.pow(tempSf, this.sn) * Math.cos(slat1) / this.sn;

        double tempRo = Math.tan(PI * 0.25 + this.olat * 0.5);
        this.ro = this.re * this.sf / Math.pow(tempRo, this.sn);

        log.info("좌표 변환 서비스 초기화 완료");
    }

    /**
     * 위도/경도를 기상청 격자좌표(X,Y)로 변환
     * @param longitude 경도
     * @param latitude 위도
     * @return GridCoordinate 객체 {x, y}
     */
    public GridCoordinate convertToGrid(double longitude, double latitude) {
        log.debug("위경도 -> 격자 변환: lon={}, lat={}", longitude, latitude);

        double ra = Math.tan(PI * 0.25 + latitude * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);

        double theta = longitude * DEGRAD - olon;
        if (theta > PI) theta -= 2.0 * PI;
        if (theta < -PI) theta += 2.0 * PI;
        theta *= sn;

        double x = ra * Math.sin(theta) + XO;
        double y = ro - ra * Math.cos(theta) + YO;

        // 격자 좌표는 정수로 반올림
        int gridX = (int) Math.round(x);
        int gridY = (int) Math.round(y);

        log.debug("변환 결과: X={}, Y={}", gridX, gridY);
        return new GridCoordinate(gridX, gridY);
    }

    /**
     * 격자 좌표 클래스
     */
    public static class GridCoordinate {
        public final int x;
        public final int y;

        public GridCoordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return String.format("Grid(x=%d, y=%d)", x, y);
        }
    }
}