package org.example.ootoutfitoftoday.domain.recommendation.controller;

import com.ootcommon.recommendation.dto.RecommendationBatchCreateResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ootoutfitoftoday.common.response.Response;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Hidden // Swagger UI에서 숨김 (내부 API이므로)
@Tag(name = "Internal API", description = "배치 서버 전용 Internal API")
public interface RecommendationInternalController {

    @Operation(
            summary = "[Internal] 배치용 추천 생성",
            description = """
                    배치 서버의 Processor에서 호출하는 Internal API입니다.
                    
                    특정 사용자에 대해 1년 이상 미착용 옷을 조회하고
                    판매/기부 추천 데이터를 생성하여 반환합니다.
                    
                    반환된 데이터는 배치 서버의 Writer에서 저장됩니다.
                    
                    주의: 이 API는 배치 서버 전용이며, 외부 접근이 제한되어야 합니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "201", description = "추천 생성 성공"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            }
    )
    ResponseEntity<Response<List<RecommendationBatchCreateResponse>>> createRecommendationsForBatch(
            Long userId
    );

    @Operation(
            summary = "[Internal] 헬스체크",
            description = "배치 서버에서 메인 서버 연결 상태를 확인합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상")
            }
    )
    ResponseEntity<Response<String>> healthCheck();
}