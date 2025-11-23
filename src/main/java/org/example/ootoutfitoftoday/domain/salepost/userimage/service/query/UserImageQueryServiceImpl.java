package org.example.ootoutfitoftoday.domain.salepost.userimage.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.salepost.userimage.entity.UserImage;
import org.example.ootoutfitoftoday.domain.salepost.userimage.exception.UserImageErrorCode;
import org.example.ootoutfitoftoday.domain.salepost.userimage.exception.UserImageException;
import org.example.ootoutfitoftoday.domain.salepost.userimage.repository.UserImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserImageQueryServiceImpl implements UserImageQueryService {

    private final UserImageRepository userImageRepository;

    @Override
    public UserImage findByIdAndIsDeletedFalse(Long userImageId) {

        return userImageRepository.findByIdAndIsDeletedFalse(userImageId).orElseThrow(() -> {
            log.warn("사용자 이미지를 찾을 수 없음 - userImageId: {}", userImageId);

            return new UserImageException(UserImageErrorCode.USER_IMAGE_NOT_FOUND);
        });
    }
}