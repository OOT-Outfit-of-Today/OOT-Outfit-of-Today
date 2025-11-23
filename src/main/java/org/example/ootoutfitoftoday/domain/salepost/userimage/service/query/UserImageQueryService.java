package org.example.ootoutfitoftoday.domain.salepost.userimage.service.query;

import org.example.ootoutfitoftoday.domain.salepost.userimage.entity.UserImage;

public interface UserImageQueryService {

    UserImage findByIdAndIsDeletedFalse(Long userImageId);
}