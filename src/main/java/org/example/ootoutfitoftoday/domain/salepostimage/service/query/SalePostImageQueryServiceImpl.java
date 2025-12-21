package org.example.ootoutfitoftoday.domain.salepostimage.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.salepostimage.entity.SalePostImage;
import org.example.ootoutfitoftoday.domain.salepostimage.repository.SalePostImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalePostImageQueryServiceImpl implements SalePostImageQueryService {

    private final SalePostImageRepository salePostImageRepository;

    @Override
    public List<SalePostImage> findBySalePostIdWithoutImage(Long salePostId) {
        return salePostImageRepository.findBySalePostIdAndIsDeletedFalseWithoutImage(salePostId);
    }

    @Override
    public List<SalePostImage> findBySalePostIdWithImage(Long salePostId) {
        return salePostImageRepository.findBySalePostIdAndIsDeletedFalseWithImage(salePostId);
    }
}
