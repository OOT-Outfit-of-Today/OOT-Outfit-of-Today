package org.example.ootoutfitoftoday.domain.recommendation.controller;

import com.ootcommon.recommendation.dto.RecommendationBatchCreateResponse;
import com.ootcommon.recommendation.type.RecommendationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;
import org.example.ootoutfitoftoday.domain.recommendation.exception.RecommendationSuccessCode;
import org.example.ootoutfitoftoday.domain.recommendation.service.command.RecommendationCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/internal/batch/recommendations")
public class RecommendationInternalControllerImpl implements RecommendationInternalController {

    private final RecommendationCommandService recommendationCommandService;

    @Override
    @PostMapping("/users/{userId}")
    public ResponseEntity<Response<List<RecommendationBatchCreateResponse>>> createRecommendationsForBatch(
            @PathVariable Long userId
    ) {
        log.info("[Internal API] 배치 추천 생성 요청 - 사용자: {}", userId);
        long startTime = System.currentTimeMillis();

        List<Recommendation> recommendations =
                recommendationCommandService.createRecommendationsForBatch(userId);

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("[Internal API] 배치 추천 생성 완료 - 생성 건수: {}, 사용자: {}, 처리시간: {}ms",
                recommendations.size(), userId, processingTime);

        if (recommendations.isEmpty()) {
            log.debug("[Internal API] 미착용 옷 없음 - 사용자: {}", userId);
        } else {
            Map<RecommendationType, Long> counts = recommendations.stream()
                    .collect(Collectors.groupingBy(Recommendation::getType, Collectors.counting()));
            log.debug("[Internal API] 추천 타입별 생성 건수 - 사용자: {}, 판매: {}, 기부: {}",
                    userId,
                    counts.getOrDefault(RecommendationType.SALE, 0L),
                    counts.getOrDefault(RecommendationType.DONATION, 0L));
        }

        List<RecommendationBatchCreateResponse> responseList = recommendations.stream()
                .map(rec -> RecommendationBatchCreateResponse.of(
                        rec.getUser().getId(),
                        rec.getClothes().getId(),
                        rec.getType().name(),
                        rec.getReason(),
                        rec.getStatus().name()
                ))
                .toList();

        return Response.success(responseList, RecommendationSuccessCode.RECOMMENDATION_CREATED);
    }

    @Override
    @GetMapping("/health")
    public ResponseEntity<Response<String>> healthCheck() {
        log.debug("[Internal API] 헬스체크 요청");

        return Response.success("Internal API is healthy", RecommendationSuccessCode.RECOMMENDATION_GET_OK);
    }
}