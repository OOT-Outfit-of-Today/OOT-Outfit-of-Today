package org.example.ootoutfitoftoday.domain.clothesImage.controller;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.clothesImage.repository.ClothesImageRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/clothes")
public class ClothesImageControllerImpl implements ClothesImageController {
    private final ClothesImageRepository clothesImageRepository;

    // 이미지 연결

    // 메인 이미지 변경

    // 이미지 제거
}
