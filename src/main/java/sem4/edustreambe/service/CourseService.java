package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sem4.edustreambe.dto.course.request.CourseCreationRequest;
import sem4.edustreambe.dto.course.request.CourseModuleRequest;
import sem4.edustreambe.dto.course.request.CourseUpdateRequest;
import sem4.edustreambe.dto.course.request.LessonRequest;
import sem4.edustreambe.dto.course.response.CourseModuleResponse;
import sem4.edustreambe.dto.course.response.CourseResponse;
import sem4.edustreambe.dto.course.response.LessonResponse;
import sem4.edustreambe.entity.*;
import sem4.edustreambe.enums.CourseStatus;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;
import sem4.edustreambe.mapper.CourseMapper;
import sem4.edustreambe.repository.CourseModuleRepository;
import sem4.edustreambe.repository.CourseRepository;
import sem4.edustreambe.repository.LessonRepository;
import sem4.edustreambe.repository.TutorProfileRepository;
import sem4.edustreambe.repository.UserRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CourseService {

    UserRepository userRepository;
    TutorProfileRepository tutorProfileRepository;
    CourseRepository courseRepository;
    CourseModuleRepository moduleRepository;
    LessonRepository lessonRepository;
    CourseMapper courseMapper;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private TutorProfile getCurrentTutorProfile() {
        User currentUser = getCurrentUser();
        return tutorProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.TUTOR_PROFILE_NOT_FOUND));
    }

    // ==========================================
    // TUTOR: COURSE MANAGEMENT
    // ==========================================

    public CourseResponse createCourse(CourseCreationRequest request) {
        TutorProfile tutorProfile = getCurrentTutorProfile();
        
        Course course = courseMapper.toCourse(request);
        course.setTutorProfile(tutorProfile);
        course.setStatus(CourseStatus.DRAFT);
        
        Course saved = courseRepository.save(course);
        log.info("Course created: [{}] by tutor: [{}]", saved.getId(), tutorProfile.getId());
        return courseMapper.toCourseResponse(saved);
    }

    public List<CourseResponse> getMyCourses() {
        TutorProfile tutorProfile = getCurrentTutorProfile();
        return courseRepository.findByTutorProfileId(tutorProfile.getId())
                .stream()
                .map(courseMapper::toCourseResponse)
                .toList();
    }

    public CourseResponse getCourseDetail(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        return courseMapper.toCourseResponse(course);
    }

    public CourseResponse updateCourse(String courseId, CourseUpdateRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
                
        verifyCourseOwnership(course);
        
        if (course.getStatus() == CourseStatus.PUBLISHED || course.getStatus() == CourseStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_COURSE_STATUS);
        }

        courseMapper.updateCourse(course, request);
        return courseMapper.toCourseResponse(courseRepository.save(course));
    }

    // ==========================================
    // TUTOR: MODULE MANAGEMENT
    // ==========================================

    public CourseModuleResponse addModule(String courseId, CourseModuleRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
        
        verifyCourseOwnership(course);
        if (course.getStatus() == CourseStatus.PUBLISHED || course.getStatus() == CourseStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_COURSE_STATUS);
        }

        CourseModule module = courseMapper.toCourseModule(request);
        module.setCourse(course);
        
        CourseModule saved = moduleRepository.save(module);
        return courseMapper.toCourseModuleResponse(saved);
    }

    // ==========================================
    // TUTOR: LESSON MANAGEMENT
    // ==========================================

    public LessonResponse addLesson(String moduleId, LessonRequest request) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new AppException(ErrorCode.MODULE_NOT_FOUND));
                
        verifyCourseOwnership(module.getCourse());
        if (module.getCourse().getStatus() == CourseStatus.PUBLISHED || module.getCourse().getStatus() == CourseStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_COURSE_STATUS);
        }

        Lesson lesson = courseMapper.toLesson(request);
        lesson.setModule(module);
        
        Lesson saved = lessonRepository.save(lesson);
        return courseMapper.toLessonResponse(saved);
    }

    // ==========================================
    // TUTOR: SUBMIT COURSE
    // ==========================================

    public CourseResponse submitCourse(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
                
        verifyCourseOwnership(course);
        
        if (course.getStatus() != CourseStatus.DRAFT && course.getStatus() != CourseStatus.REJECTED) {
            throw new AppException(ErrorCode.INVALID_COURSE_STATUS);
        }

        if (course.getModules() == null || course.getModules().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_COURSE_STATUS); // Khóa học phải có ít nhất 1 module
        }

        course.setStatus(CourseStatus.PENDING);
        return courseMapper.toCourseResponse(courseRepository.save(course));
    }

    // ==========================================
    // ADMIN: REVIEW COURSE
    // ==========================================

    public List<CourseResponse> getPendingCourses() {
        return courseRepository.findByStatus(CourseStatus.PENDING)
                .stream()
                .map(courseMapper::toCourseResponse)
                .toList();
    }

    public CourseResponse reviewCourse(String courseId, boolean isApprove) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
                
        if (course.getStatus() != CourseStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_COURSE_STATUS);
        }

        if (isApprove) {
            course.setStatus(CourseStatus.PUBLISHED);
        } else {
            course.setStatus(CourseStatus.REJECTED);
        }

        return courseMapper.toCourseResponse(courseRepository.save(course));
    }

    private void verifyCourseOwnership(Course course) {
        TutorProfile tutorProfile = getCurrentTutorProfile();
        if (!course.getTutorProfile().getId().equals(tutorProfile.getId())) {
            throw new AppException(ErrorCode.COURSE_OWNERSHIP_DENIED);
        }
    }
}
