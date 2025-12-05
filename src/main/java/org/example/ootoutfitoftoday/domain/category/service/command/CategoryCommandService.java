package org.example.ootoutfitoftoday.domain.category.service.command;

import org.example.ootoutfitoftoday.domain.category.dto.request.CategoryRequest;
import org.example.ootoutfitoftoday.domain.category.dto.response.CategoryResponse;

public interface CategoryCommandService {

    CategoryResponse createCategory(CategoryRequest categoryRequest);

    CategoryResponse updateCategory(Long categoryId, CategoryRequest categoryRequest);

    void deleteCategory(Long categoryId);
}
