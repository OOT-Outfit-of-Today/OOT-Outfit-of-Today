package org.example.ootoutfitoftoday.domain.salepost.service.query;

import com.ootcommon.salepost.enums.SaleStatus;
import com.ootcommon.salepost.response.SaleStatusCount;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.util.DefaultLocationConstants;
import org.example.ootoutfitoftoday.common.util.Location;
import org.example.ootoutfitoftoday.common.util.PointFormatAndParse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.*;
import org.example.ootoutfitoftoday.domain.salepost.entity.SalePost;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostErrorCode;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostException;
import org.example.ootoutfitoftoday.domain.salepost.repository.SalePostRepository;
import org.example.ootoutfitoftoday.domain.salepost.service.cache.SalePostCacheService;
import org.example.ootoutfitoftoday.domain.salepost.util.NativeQuerySortUtil;
import org.example.ootoutfitoftoday.domain.salepost.util.SliceContent;
import org.example.ootoutfitoftoday.domain.salepostimage.entity.SalePostImage;
import org.example.ootoutfitoftoday.domain.salepostimage.repository.SalePostImageRepository;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalePostQueryServiceImpl implements SalePostQueryService {

    private final SalePostRepository salePostRepository;
    private final SalePostCacheService salePostCacheService;
    private final UserQueryService userQueryService;
    private final EntityManager entityManager;
    private final SalePostImageRepository salePostImageRepository;

    private static SliceContent sliceAndQueryResult(Query query, Pageable pageable) {
        int offset = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize() + 1;

        query.setFirstResult(offset);
        query.setMaxResults(limit);

        @SuppressWarnings("unchecked")
        List<SalePost> results = query.getResultList();

        boolean hasNext = results.size() > pageable.getPageSize();
        List<SalePost> content = hasNext ?
                results.subList(0, pageable.getPageSize()) :
                results;

        return SliceContent.from(content, hasNext);
    }

    @Override
    public SalePost findSalePostById(Long salePostId) {

        return salePostRepository.findByIdAsNativeQuery(salePostId)
                .orElseThrow(() -> new SalePostException(SalePostErrorCode.SALE_POST_NOT_FOUND));
    }

    @Override
    public SalePostDetailResponse getSalePostDetail(Long salePostId) {

        SalePost salePost = findSalePostById(salePostId);

        // 이미지 별도 조회
        List<SalePostImage> salePostImages = salePostImageRepository.findBySalePostIdWithImage(salePostId);

        // 이미지 포함하여 Response 생성
        return SalePostDetailResponse.fromWithImages(salePost, salePostImages);
    }

    @Override
    public Slice<SalePostListResponse> getSalePostList(
            Long userId,
            Long categoryId,
            SaleStatus status,
            String keyword,
            Pageable pageable
    ) {
        log.info("SalePostService.getSalePostList : userId={}, categoryId={}", userId, categoryId);

        User user = userQueryService.findByIdAsNativeQuery(userId);

        Location location = PointFormatAndParse.parse(user.getTradeLocation());

        final BigDecimal LOCATION_PRECISION_FACTOR = new BigDecimal("10000");

        BigDecimal conversionLatitude = location.latitude().multiply(LOCATION_PRECISION_FACTOR);
        BigDecimal conversionLongitude = location.longitude().multiply(LOCATION_PRECISION_FACTOR);

        Long cacheKeyLatitude = conversionLatitude.setScale(0, java.math.RoundingMode.HALF_UP).longValue();
        Long cacheKeyLongitude = conversionLongitude.setScale(0, java.math.RoundingMode.HALF_UP).longValue();

        CachedSliceResponse<SalePostListResponse> cached = salePostCacheService.getCachedSalePostList(
                user, cacheKeyLatitude, cacheKeyLongitude, categoryId, status, keyword, pageable
        );

        return cached.toSlice();
    }

    @Override
    public long countByIsDeletedFalse() {

        return salePostRepository.countByIsDeletedFalse();
    }

    @Override
    public List<SaleStatusCount> saleStatusCounts() {

        return salePostRepository.saleStatusCounts();
    }

    @Override
    public int countSalePostsRegisteredSince(LocalDateTime start, LocalDateTime end) {

        return salePostRepository.countSalePostsRegisteredSince(start, end);
    }

    @Override
    public Slice<SalePostListResponse> findMySalePosts(
            Long userId,
            SaleStatus status,
            Pageable pageable
    ) {
        // Native Query 수정(이미지 서브쿼리 추가)
        String baseSql = """
                SELECT
                    s.id,
                    s.title,
                    s.content,
                    s.price,
                    s.status,
                    s.trade_address,
                    ST_AsText(s.trade_location) AS trade_location,
                    s.user_id,
                    s.category_id,
                    s.recommendation_id,
                    s.created_at,
                    s.updated_at,
                    s.is_deleted,
                    s.deleted_at,
                    (SELECT i.url
                     FROM sale_post_images spi
                     JOIN images i ON spi.image_id = i.id
                     WHERE spi.sale_post_id = s.id
                     AND spi.is_main = TRUE
                     AND spi.is_deleted = FALSE
                     LIMIT 1) AS thumbnail_url
                FROM sale_posts s
                WHERE s.is_deleted = FALSE
                AND s.user_id = :userId
                AND (:status IS NULL OR s.status = :status)
                """;

        String finalSql = NativeQuerySortUtil.buildOrderClause(baseSql, pageable);

        Query query = entityManager.createNativeQuery(finalSql);

        query.setParameter("userId", userId);
        query.setParameter("status", status != null ? status.name() : null);

        int offset = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize() + 1;

        query.setFirstResult(offset);
        query.setMaxResults(limit);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        boolean hasNext = results.size() > pageable.getPageSize();
        List<Object[]> content = hasNext ?
                results.subList(0, pageable.getPageSize()) :
                results;

        // Object[] 매핑(thumbnailUrl 포함)
        List<SalePostListResponse> responseContent = content.stream()
                .map(this::mapToSalePostSummaryResponse)
                .toList();

        return new SliceImpl<>(responseContent, pageable, hasNext);
    }

    // 추가: Object[] -> SalePostSummaryResponse 매핑
    // 설명: Native Query 결과를 Response DTO로 변환
    private SalePostListResponse mapToSalePostSummaryResponse(Object[] row) {
        String tradeLocationStr = (String) row[6];
        Location location = PointFormatAndParse.parse(tradeLocationStr);

        String thumbnailUrl = row.length > 14 ? (String) row[14] : null;    // 썸네일

        return SalePostListResponse.builder()
                .salePostId(((Number) row[0]).longValue())
                .title((String) row[1])
                .price((BigDecimal) row[3])
                .status(SaleStatus.valueOf((String) row[4]))
                .tradeAddress((String) row[5])
                .tradeLatitude(location.latitude())
                .tradeLongitude(location.longitude())
                .thumbnailUrl(thumbnailUrl)    // 외부에서 받은 값
                .createdAt(((java.sql.Timestamp) row[10]).toLocalDateTime())
                .build();
    }

    @Override
    public Optional<SalePost> findByRecommendationId(Long recommendationId) {

        return salePostRepository.findByRecommendationIdAndIsDeletedFalse(recommendationId);
    }

    @Override
    public Slice<SalePostListResponse> getNotAuthSalePostList(
            Long categoryId,
            SaleStatus status,
            String keyword,
            Pageable pageable
    ) {
        // Native Query 수정(이미지 서브쿼리)
        String baseSql = """
                SELECT
                    s.id,
                    s.title,
                    s.price,
                    s.status,
                    s.trade_address,
                    ST_AsText(s.trade_location) AS trade_location,
                    (SELECT i.url
                     FROM sale_post_images spi
                     JOIN images i ON spi.image_id = i.id
                     WHERE spi.sale_post_id = s.id
                     AND spi.is_main = TRUE
                     AND spi.is_deleted = FALSE
                     LIMIT 1) AS thumbnail_url,
                    u.nickname AS seller_nickname,
                    c.name AS category_name,
                    s.created_at
                FROM sale_posts s
                JOIN users u ON s.user_id = u.id
                JOIN categories c ON s.category_id = c.id
                WHERE s.is_deleted = FALSE
                AND ST_Distance_Sphere(
                                  s.trade_location,
                                  ST_GeomFromText(:defaultPoint, 4326)
                              ) <= (:km * 1000)
                AND (:categoryId IS NULL OR s.category_id = :categoryId)
                AND (:status IS NULL OR s.status = :status)
                AND (:keyword IS NULL OR s.title LIKE :keyword OR s.content LIKE :keyword)
                """;

        String finalSql = NativeQuerySortUtil.buildOrderClause(baseSql, pageable);

        Query query = entityManager.createNativeQuery(finalSql);

        query.setParameter("defaultPoint", DefaultLocationConstants.DEFAULT_TRADE_LOCATION);
        query.setParameter("km", DefaultLocationConstants.KM);
        query.setParameter("categoryId", categoryId);
        query.setParameter("status", status != null ? status.name() : null);

        String searchKeyword = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            searchKeyword = "%" + keyword.trim() + "%";
        }
        query.setParameter("keyword", searchKeyword);

        int offset = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize() + 1;

        query.setFirstResult(offset);
        query.setMaxResults(limit);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        boolean hasNext = results.size() > pageable.getPageSize();
        List<Object[]> content = hasNext ?
                results.subList(0, pageable.getPageSize()) :
                results;

        List<SalePostListResponse> responseContent = content.stream()
                .map(this::mapToSalePostPublicListResponse)
                .toList();

        return new SliceImpl<>(responseContent, pageable, hasNext);
    }

    private SalePostListResponse mapToSalePostPublicListResponse(Object[] row) {
        String tradeLocationStr = (String) row[5];
        Location location = PointFormatAndParse.parse(tradeLocationStr);

        return SalePostListResponse.builder()
                .salePostId(((Number) row[0]).longValue())
                .title((String) row[1])
                .price((BigDecimal) row[2])
                .status(SaleStatus.valueOf((String) row[3]))
                .tradeAddress((String) row[4])
                .tradeLatitude(location.latitude())
                .tradeLongitude(location.longitude())
                .thumbnailUrl((String) row[6])    // 서브쿼리에서 조회한 URL
                .sellerNickname((String) row[7])
                .categoryName((String) row[8])
                .createdAt(((java.sql.Timestamp) row[9]).toLocalDateTime())
                .build();
    }
}