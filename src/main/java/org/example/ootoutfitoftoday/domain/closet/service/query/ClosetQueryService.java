package org.example.ootoutfitoftoday.domain.closet.service.query;

import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetGetResponse;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.springframework.data.domain.Page;

public interface ClosetQueryService {

    // 내 옷장(비공개 포함)
    Page<ClosetGetResponse> getMyClosets(
            Long userId,
            int page,
            int size,
            String sort,
            String direction
    );

    // 공개 옷장(특정 유저 or 전체)
    Page<ClosetGetResponse> getPublicClosets(
            Long userId,
            int page,
            int size,
            String sort,
            String direction
    );

    // 단건
    ClosetGetResponse getMyCloset(Long userId, Long closetId);

    // 공개 옷장 단건
    ClosetGetResponse getPublicCloset(Long closetId);

    // 옷장과 옷을 연결할 때, 사용할 옷장 조회
    Closet findClosetByIdAndUserIdAndIsDeletedFalse(Long userId, Long closetId);
}
