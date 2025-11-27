package org.example.ootoutfitoftoday.domain.closet.service.query;

import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetGetResponse;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.springframework.data.domain.Page;

public interface ClosetQueryService {

    // 내 옷장(비공개 포함)
    Page<ClosetGetResponse> getMyClosets(
            Long loginUserId,
            int page,
            int size,
            String sort,
            String direction
    );

    // 공개 옷장(특정 유저 or 전체)
    Page<ClosetGetResponse> getPublicClosets(
            Long targetUserId,
            int page,
            int size,
            String sort,
            String direction
    );

    ClosetGetResponse getCloset(Long closetId);

    Closet findClosetById(Long closetId);
}
