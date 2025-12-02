package org.example.ootoutfitoftoday.domain.donation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.donation.dto.response.DonationCenterSearchResponse;
import org.example.ootoutfitoftoday.domain.donation.exception.DonationSuccessCode;
import org.example.ootoutfitoftoday.domain.donation.service.query.DonationCenterQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/donation-centers")
public class DonationCenterControllerImpl implements DonationCenterController {

    private final DonationCenterQueryService donationCenterQueryService;

    @Override
    @GetMapping("/search")
    public ResponseEntity<Response<List<DonationCenterSearchResponse>>> searchNearbyDonationCenters(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) Integer radius,
            @RequestParam(required = false) String keyword
    ) {
        log.info("주변 기부처 검색 요청 - 위도: {}, 경도: {}, 반경: {}m, 키워드: {}",
                latitude, longitude, radius, keyword);
        long startTime = System.currentTimeMillis();

        List<DonationCenterSearchResponse> donationCenters = donationCenterQueryService.searchNearbyDonationCenters(
                latitude,
                longitude,
                radius,
                keyword
        );

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("주변 기부처 검색 완료 - 검색 건수: {}, 처리시간: {}ms", donationCenters.size(), processingTime);

        if (!donationCenters.isEmpty()) {
            log.debug("검색 결과 요약 - 최근접 기부처: {}, 거리: {}m",
                    donationCenters.get(0).name(),
                    donationCenters.get(0).distance());
        }

        return Response.success(donationCenters, DonationSuccessCode.DONATION_CENTER_SEARCH_SUCCESS);
    }
}