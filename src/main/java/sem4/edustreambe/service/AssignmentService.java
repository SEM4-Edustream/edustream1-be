package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sem4.edustreambe.dto.common.PageMeta;
import sem4.edustreambe.dto.assignment.request.AssignmentGradeRequest;
import sem4.edustreambe.dto.assignment.request.AssignmentSubmissionRequest;
import sem4.edustreambe.dto.assignment.response.AssignmentSubmissionResponse;
import sem4.edustreambe.entity.AssignmentSubmission;
import sem4.edustreambe.entity.Lesson;
import sem4.edustreambe.entity.User;
import sem4.edustreambe.enums.AssignmentStatus;
import sem4.edustreambe.enums.LessonType;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;
import sem4.edustreambe.mapper.AssignmentMapper;
import sem4.edustreambe.repository.AssignmentSubmissionRepository;
import sem4.edustreambe.repository.LessonRepository;
import sem4.edustreambe.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AssignmentService {

    AssignmentSubmissionRepository assignmentSubmissionRepository;
    LessonRepository lessonRepository;
    UserRepository userRepository;
    AssignmentMapper assignmentMapper;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public AssignmentSubmissionResponse submitAssignment(String lessonId, AssignmentSubmissionRequest request) {
        User student = getCurrentUser();
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        if (lesson.getType() != LessonType.ASSIGNMENT) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION); // Could create specific error
        }

        AssignmentSubmission submission = assignmentSubmissionRepository.findByLessonIdAndStudentId(lessonId, student.getId())
                .orElse(AssignmentSubmission.builder()
                        .lesson(lesson)
                        .student(student)
                        .status(AssignmentStatus.SUBMITTED)
                        .build());

        // Allow resubmission if not yet graded
        if (submission.getStatus() == AssignmentStatus.GRADED) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION); // Cannot resubmit graded assignment
        }

        submission.setContent(request.getContent());
        submission.setFileUrl(request.getFileUrl());
        submission.setStatus(AssignmentStatus.SUBMITTED);

        AssignmentSubmission saved = assignmentSubmissionRepository.save(submission);
        return assignmentMapper.toAssignmentSubmissionResponse(saved);
    }

    public AssignmentSubmissionResponse getMySubmission(String lessonId) {
        User student = getCurrentUser();
        AssignmentSubmission submission = assignmentSubmissionRepository.findByLessonIdAndStudentId(lessonId, student.getId())
                .orElse(null);

        if (submission == null) {
            return null; // No submission yet
        }

        return assignmentMapper.toAssignmentSubmissionResponse(submission);
    }

    public PageMeta<AssignmentSubmissionResponse> getSubmissionsForTutor(String lessonId, Pageable pageable) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        if (lesson.getType() != LessonType.ASSIGNMENT) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        // Verify tutor owns the course
        User tutor = getCurrentUser();
        if (!lesson.getModule().getCourse().getTutorProfile().getUser().getId().equals(tutor.getId()) && !tutor.getRole().getName().equals("ROLE_ADMIN")) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Page<AssignmentSubmission> page = assignmentSubmissionRepository.findByLessonId(lessonId, pageable);

        Page<AssignmentSubmissionResponse> responsePage = page.map(assignmentMapper::toAssignmentSubmissionResponse);

        return PageMeta.<AssignmentSubmissionResponse>builder()
                .content(responsePage.getContent())
                .pageNumber(responsePage.getNumber())
                .pageSize(responsePage.getSize())
                .totalElements(responsePage.getTotalElements())
                .totalPages(responsePage.getTotalPages())
                .build();
    }

    public AssignmentSubmissionResponse gradeSubmission(String submissionId, AssignmentGradeRequest request) {
        AssignmentSubmission submission = assignmentSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION)); // Assignment submission not found

        // Verify tutor owns the course
        User tutor = getCurrentUser();
        if (!submission.getLesson().getModule().getCourse().getTutorProfile().getUser().getId().equals(tutor.getId()) && !tutor.getRole().getName().equals("ROLE_ADMIN")) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        submission.setGrade(request.getGrade());
        submission.setFeedback(request.getFeedback());
        submission.setStatus(AssignmentStatus.GRADED);

        AssignmentSubmission saved = assignmentSubmissionRepository.save(submission);
        return assignmentMapper.toAssignmentSubmissionResponse(saved);
    }
}
