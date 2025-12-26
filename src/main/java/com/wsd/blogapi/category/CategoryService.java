package com.wsd.blogapi.category;

import com.wsd.blogapi.category.dto.CategoryResponse;
import com.wsd.blogapi.category.dto.CreateCategoryRequest;
import com.wsd.blogapi.category.dto.UpdateCategoryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 카테고리 생성 (관리자 전용)
     */
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        // 중복 체크
        if (categoryRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("이미 존재하는 카테고리명입니다");
        }
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new IllegalArgumentException("이미 존재하는 슬러그입니다");
        }

        Category category = new Category(
                request.name(),
                request.slug(),
                request.description()
        );

        Category savedCategory = categoryRepository.save(category);
        return CategoryResponse.from(savedCategory);
    }

    /**
     * 모든 카테고리 목록 조회
     */
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    /**
     * 카테고리 단건 조회 (ID)
     */
    public CategoryResponse getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다"));
        return CategoryResponse.from(category);
    }

    /**
     * 카테고리 단건 조회 (Slug)
     */
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다"));
        return CategoryResponse.from(category);
    }

    /**
     * 카테고리 수정 (관리자 전용)
     */
    @Transactional
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다"));

        // 이름 수정 시 중복 체크
        if (request.name() != null && !request.name().equals(category.getName())) {
            if (categoryRepository.existsByName(request.name())) {
                throw new IllegalArgumentException("이미 존재하는 카테고리명입니다");
            }
            category.updateName(request.name());
        }

        // 슬러그 수정 시 중복 체크
        if (request.slug() != null && !request.slug().equals(category.getSlug())) {
            if (categoryRepository.existsBySlug(request.slug())) {
                throw new IllegalArgumentException("이미 존재하는 슬러그입니다");
            }
            category.updateSlug(request.slug());
        }

        if (request.description() != null) {
            category.updateDescription(request.description());
        }

        return CategoryResponse.from(category);
    }

    /**
     * 카테고리 삭제 (관리자 전용)
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다"));

        categoryRepository.delete(category);
    }
}
