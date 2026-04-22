package sem4.edustreambe.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sem4.edustreambe.dto.course.request.CourseCreationRequest;
import sem4.edustreambe.dto.course.request.CourseModuleRequest;
import sem4.edustreambe.dto.course.request.CourseUpdateRequest;
import sem4.edustreambe.dto.course.request.LessonRequest;
import sem4.edustreambe.dto.course.response.CourseModuleResponse;
import sem4.edustreambe.dto.course.response.CourseResponse;
import sem4.edustreambe.dto.course.response.LessonResponse;
import sem4.edustreambe.entity.Course;
import sem4.edustreambe.entity.CourseModule;
import sem4.edustreambe.entity.Lesson;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CourseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tutorProfile", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "modules", ignore = true)
    Course toCourse(CourseCreationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tutorProfile", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "modules", ignore = true)
    void updateCourse(@MappingTarget Course course, CourseUpdateRequest request);

    @Mapping(target = "tutorProfileId", source = "tutorProfile.id")
    @Mapping(target = "tutorName", source = "tutorProfile.user.fullName")
    CourseResponse toCourseResponse(Course course);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    CourseModule toCourseModule(CourseModuleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    void updateCourseModule(@MappingTarget CourseModule module, CourseModuleRequest request);

    CourseModuleResponse toCourseModuleResponse(CourseModule module);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "module", ignore = true)
    Lesson toLesson(LessonRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "module", ignore = true)
    void updateLesson(@MappingTarget Lesson lesson, LessonRequest request);

    LessonResponse toLessonResponse(Lesson lesson);
}
