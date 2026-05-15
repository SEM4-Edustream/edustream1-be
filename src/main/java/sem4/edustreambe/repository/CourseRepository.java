package sem4.edustreambe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.Course;
import sem4.edustreambe.enums.CourseStatus;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
    List<Course> findByTutorProfileId(String tutorProfileId);
    List<Course> findByStatus(CourseStatus status);

    // Dành cho Tutor Dashboard & Pagination
    org.springframework.data.domain.Page<Course> findByTutorProfileId(String tutorProfileId, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Course> findByTutorProfileIdAndStatus(String tutorProfileId, CourseStatus status, org.springframework.data.domain.Pageable pageable);

    // Dành cho Public Search & Pagination
    org.springframework.data.domain.Page<Course> findByStatus(CourseStatus status, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Course> findByStatusAndTitleContainingIgnoreCase(CourseStatus status, String title, org.springframework.data.domain.Pageable pageable);

    // Category filtering (Recursive for sub-categories)
    @org.springframework.data.jpa.repository.Query("SELECT c FROM Course c WHERE c.status = :status AND " +
           "(c.category.slug = :categorySlug OR c.category.parent.slug = :categorySlug)")
    org.springframework.data.domain.Page<Course> findByStatusAndCategoryOrParentCategory(
            @org.springframework.data.repository.query.Param("status") CourseStatus status, 
            @org.springframework.data.repository.query.Param("categorySlug") String categorySlug, 
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Course c WHERE c.status = :status AND " +
           "(c.category.slug = :categorySlug OR c.category.parent.slug = :categorySlug) AND " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    org.springframework.data.domain.Page<Course> findByStatusAndCategoryOrParentCategoryAndTitleContainingIgnoreCase(
            @org.springframework.data.repository.query.Param("status") CourseStatus status, 
            @org.springframework.data.repository.query.Param("categorySlug") String categorySlug, 
            @org.springframework.data.repository.query.Param("title") String title, 
            org.springframework.data.domain.Pageable pageable);

    // Dành cho Admin: Hiện tất cả trừ DRAFT
    org.springframework.data.domain.Page<Course> findByStatusNot(CourseStatus status, org.springframework.data.domain.Pageable pageable);
}
