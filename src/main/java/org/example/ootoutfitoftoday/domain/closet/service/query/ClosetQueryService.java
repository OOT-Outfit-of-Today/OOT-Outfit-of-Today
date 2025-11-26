package org.example.ootoutfitoftoday.domain.closet.service.query;

import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetGetResponse;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.springframework.data.domain.Page;

public interface ClosetQueryService {

    Page<ClosetGetResponse> getClosets(
            Long loginUserId,
            Long targetUserId,
            int page,
            int size,
            String sort,
            String direction
    );

    ClosetGetResponse getCloset(Long closetId);

    Closet findClosetById(Long closetId);
}
