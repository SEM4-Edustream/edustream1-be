package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sem4.edustreambe.entity.*;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;
import sem4.edustreambe.repository.*;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProgressService {

    UserRepository userRepository;
    LessonRepository lessonRepository;
    LessonProgressRepository lessonProgressRepository;
    EnrollmentRepository enrollmentRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    @Transactional
    public void markLessonComplete(String lessonId) {
        User student = getCurrentUser();

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        Course course = lesson.getModule().getCourse();

        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(student.getId(), course.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        if (lessonProgressRepository.findByUserIdAndLessonId(student.getId(), lessonId).isPresent()) {
            throw new AppException(ErrorCode.LESSON_ALREADY_COMPLETED);
        }

        LessonProgress progress = LessonProgress.builder()
                .user(student)
                .lesson(lesson)
                .isCompleted(true)
                .completedAt(LocalDateTime.now())
                .build();
        lessonProgressRepository.save(progress);

        recalculateEnrollmentProgress(enrollment, student, course);
    }

    private void recalculateEnrollmentProgress(Enrollment enrollment, User student, Course course) {
        long totalLessons = lessonRepository.countByModule_Course(course);
        if (totalLessons == 0) {
            enrollment.setProgressPercentage(100);
        } else {
            long completedLessons = lessonProgressRepository.countByUserAndLesson_Module_CourseAndIsCompletedTrue(student, course);
            int newPercentage = (int) ((completedLessons * 100) / totalLessons);
            enrollment.setProgressPercentage(Math.min(newPercentage, 100));
        }
        enrollmentRepository.save(enrollment);
    }
}
