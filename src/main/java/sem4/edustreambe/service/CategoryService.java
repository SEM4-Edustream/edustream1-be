package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import sem4.edustreambe.dto.course.response.CategoryResponse;
import sem4.edustreambe.entity.Category;
import sem4.edustreambe.mapper.CategoryMapper;
import sem4.edustreambe.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryService {
    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .filter(category -> category.getParent() == null)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CategoryResponse mapToResponse(Category category) {
        CategoryResponse response = categoryMapper.toCategoryResponse(category);
        if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            response.setSubCategories(category.getSubCategories().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList()));
        }
        return response;
    }
    
    // Thêm phương thức hỗ trợ cho Admin/Seeding nếu cần
    public Category getCategoryEntity(String id) {
        return categoryRepository.findById(id).orElse(null);
    }
}
