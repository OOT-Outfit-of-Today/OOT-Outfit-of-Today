package org.example.ootoutfitoftoday.common.util;

import java.math.BigDecimal;

public class PointFormatAndParse {

    // WKT 표준 순서인 POINT(경도, 위도)와 달리
    // MySQL SRID 4326은 POINT(위도, 경도) 순서
    public static String format(BigDecimal tradeLatitude, BigDecimal tradeLongitude) {

        String result = String.format("POINT(%s %s)",
                tradeLatitude,    // 위도(latitude) 먼저
                tradeLongitude    // 경도(longitude) 나중
        );

        return result;
    }

    public static Location parse(String tradeLocation) {
        String coordinateLocation = tradeLocation.substring(6, tradeLocation.length() - 1).trim();

        String[] location = coordinateLocation.split(" ");

        String latitudeStr = location[0];     // 위도
        String longitudeStr = location[1];    // 경도

        BigDecimal tradeLatitude = new BigDecimal(latitudeStr);
        BigDecimal tradeLongitude = new BigDecimal(longitudeStr);

        return Location.of(tradeLatitude, tradeLongitude);
    }
}
