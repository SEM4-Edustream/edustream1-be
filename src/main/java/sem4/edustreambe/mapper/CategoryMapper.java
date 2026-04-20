package sem4.edustreambe.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sem4.edustreambe.dto.course.response.CategoryResponse;
import sem4.edustreambe.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "subCategories", ignore = true) // Sẽ xử lý đệ quy nếu cần trong service
    CategoryResponse toCategoryResponse(Category category);
}
