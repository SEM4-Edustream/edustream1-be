package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sem4.edustreambe.dto.qa.request.AnswerRequest;
import sem4.edustreambe.dto.qa.request.QuestionRequest;
import sem4.edustreambe.dto.qa.response.AnswerResponse;
import sem4.edustreambe.dto.qa.response.QuestionResponse;
import sem4.edustreambe.entity.*;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;
import sem4.edustreambe.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QAService {

    QuestionRepository questionRepository;
    QuestionAnswerRepository questionAnswerRepository;
    CourseRepository courseRepository;
    LessonRepository lessonRepository;
    UserRepository userRepository;
    EnrollmentRepository enrollmentRepository;
    TutorProfileRepository tutorProfileRepository;

    // ─────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private List<String> getTutorCourseIds(User user) {
        TutorProfile tutorProfile = tutorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.TUTOR_PROFILE_NOT_FOUND));
        return courseRepository.findByTutorProfileId(tutorProfile.getId())
                .stream()
                .map(Course::getId)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    //  Tutor: Xem Q&A của các khóa học mình dạy
    // ─────────────────────────────────────────────

    public Page<QuestionResponse> getTutorQuestions(String courseId, String filter, Pageable pageable) {
        User tutor = getCurrentUser();
        List<String> courseIds = getTutorCourseIds(tutor);

        if (courseIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // Lọc theo khóa học cụ thể nếu có
        List<String> targetCourseIds = (courseId != null && courseIds.contains(courseId))
                ? List.of(courseId)
                : courseIds;

        Page<Question> questions;
        if ("NO_ANSWER".equals(filter)) {
            questions = questionRepository.findNoAnswerByCourseIds(targetCourseIds, pageable);
        } else if ("NO_INSTRUCTOR_ANSWER".equals(filter)) {
            questions = questionRepository.findNoInstructorAnswerByCourseIds(targetCourseIds, pageable);
        } else {
            questions = questionRepository.findByCourseIdsOrderByCreatedAtDesc(targetCourseIds, pageable);
        }

        return questions.map(q -> mapToResponse(q, false));
    }

    // ─────────────────────────────────────────────
    //  Public/Student: Xem Q&A theo khóa học
    // ─────────────────────────────────────────────

    public Page<QuestionResponse> getCourseQuestions(String courseId, Pageable pageable) {
        if (!courseRepository.existsById(courseId)) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        return questionRepository
                .findByCourseIdAndIsDeletedFalseOrderByCreatedAtDesc(courseId, pageable)
                .map(q -> mapToResponse(q, false));
    }

    // ─────────────────────────────────────────────
    //  Xem chi tiết 1 câu hỏi + toàn bộ câu trả lời
    // ─────────────────────────────────────────────

    public QuestionResponse getQuestionDetail(String questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
        return mapToResponse(question, true);
    }

    // ─────────────────────────────────────────────
    //  Student: Đặt câu hỏi
    // ─────────────────────────────────────────────

    public QuestionResponse createQuestion(String courseId, QuestionRequest request) {
        User student = getCurrentUser();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // Phải enrolled mới được hỏi
        boolean hasEnrolled = enrollmentRepository.existsByUserIdAndCourseId(student.getId(), courseId);
        if (!hasEnrolled) {
            throw new AppException(ErrorCode.MUST_ENROLL_TO_ASK);
        }

        Lesson lesson = null;
        if (request.getLessonId() != null) {
            lesson = lessonRepository.findById(request.getLessonId()).orElse(null);
        }

        Question question = Question.builder()
                .course(course)
                .lesson(lesson)
                .student(student)
                .title(request.getTitle())
                .body(request.getBody())
                .build();

        return mapToResponse(questionRepository.save(question), false);
    }

    // ─────────────────────────────────────────────
    //  Trả lời câu hỏi (cả Tutor lẫn Student)
    // ─────────────────────────────────────────────

    public AnswerResponse createAnswer(String questionId, AnswerRequest request) {
        User author = getCurrentUser();

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        // Kiểm tra xem người trả lời có phải là tutor của khóa học không
        TutorProfile tutorProfile = tutorProfileRepository.findByUserId(author.getId()).orElse(null);
        boolean isInstructor = tutorProfile != null 
                && question.getCourse().getTutorProfile() != null
                && question.getCourse().getTutorProfile().getId().equals(tutorProfile.getId());

        if (!isInstructor) {
            throw new AppException(ErrorCode.UNAUTHORIZED); // Chỉ giảng viên mới được trả lời
        }

        QuestionAnswer answer = QuestionAnswer.builder()
                .question(question)
                .author(author)
                .body(request.getBody())
                .isInstructorAnswer(true)
                .build();

        QuestionAnswer saved = questionAnswerRepository.save(answer);

        // Cập nhật số lượng câu trả lời
        question.setAnswerCount(question.getAnswerCount() + 1);
        questionRepository.save(question);

        // GỬI THÔNG BÁO CHO HỌC VIÊN
        notificationService.sendNotification(
            question.getStudent(),
            "Câu hỏi của bạn đã được trả lời",
            "Giảng viên " + author.getFullName() + " đã trả lời câu hỏi: " + question.getTitle(),
            sem4.edustreambe.enums.NotificationType.Q_AND_A,
            "/learning/" + question.getCourse().getId() + "?qa=" + question.getId()
        );

        return mapAnswerToResponse(saved);
    }

    // ─────────────────────────────────────────────
    //  Tutor: Đánh dấu Top Answer
    // ─────────────────────────────────────────────

    public AnswerResponse markTopAnswer(String answerId) {
        User tutor = getCurrentUser();

        QuestionAnswer answer = questionAnswerRepository.findById(answerId)
                .orElseThrow(() -> new AppException(ErrorCode.ANSWER_NOT_FOUND));

        // Chỉ tutor của khóa học mới được đánh dấu top answer
        TutorProfile tutorProfile = tutorProfileRepository.findByUserId(tutor.getId())
                .orElseThrow(() -> new AppException(ErrorCode.TUTOR_PROFILE_NOT_FOUND));

        Course course = answer.getQuestion().getCourse();
        if (course.getTutorProfile() == null || !course.getTutorProfile().getId().equals(tutorProfile.getId())) {
            throw new AppException(ErrorCode.QUESTION_OWNERSHIP_DENIED);
        }

        // Toggle top answer
        answer.setIsTopAnswer(!answer.getIsTopAnswer());
        return mapAnswerToResponse(questionAnswerRepository.save(answer));
    }

    // ─────────────────────────────────────────────
    //  Đánh dấu câu hỏi đã được giải quyết
    // ─────────────────────────────────────────────

    public QuestionResponse resolveQuestion(String questionId) {
        User user = getCurrentUser();

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        // Chỉ người đặt câu hỏi hoặc tutor của khóa học mới được resolve
        boolean isOwner = question.getStudent().getId().equals(user.getId());
        boolean isTutor = tutorProfileRepository.findByUserId(user.getId())
                .map(tp -> question.getCourse().getTutorProfile() != null
                        && question.getCourse().getTutorProfile().getId().equals(tp.getId()))
                .orElse(false);

        if (!isOwner && !isTutor) {
            throw new AppException(ErrorCode.QUESTION_OWNERSHIP_DENIED);
        }

        question.setIsResolved(!question.getIsResolved());
        return mapToResponse(questionRepository.save(question), false);
    }

    // ─────────────────────────────────────────────
    //  Mappers
    // ─────────────────────────────────────────────

    private QuestionResponse mapToResponse(Question q, boolean includeAnswers) {
        QuestionResponse.QuestionResponseBuilder builder = QuestionResponse.builder()
                .id(q.getId())
                .courseId(q.getCourse().getId())
                .courseTitle(q.getCourse().getTitle())
                .lessonId(q.getLesson() != null ? q.getLesson().getId() : null)
                .lessonTitle(q.getLesson() != null ? q.getLesson().getTitle() : null)
                .studentId(q.getStudent().getId().toString())
                .studentName(q.getStudent().getFullName() != null ? q.getStudent().getFullName() : q.getStudent().getUsername())
                .studentAvatar("https://ui-avatars.com/api/?name=" + q.getStudent().getUsername())
                .title(q.getTitle())
                .body(q.getBody())
                .isResolved(q.getIsResolved())
                .answerCount(q.getAnswerCount())
                .createdAt(q.getCreatedAt());

        if (includeAnswers) {
            List<AnswerResponse> answers = questionAnswerRepository
                    .findByQuestionIdAndIsDeletedFalseOrderByIsTopAnswerDescCreatedAtAsc(q.getId())
                    .stream()
                    .map(this::mapAnswerToResponse)
                    .collect(Collectors.toList());
            builder.answers(answers);
        }

        return builder.build();
    }

    private AnswerResponse mapAnswerToResponse(QuestionAnswer a) {
        return AnswerResponse.builder()
                .id(a.getId())
                .questionId(a.getQuestion().getId())
                .authorId(a.getAuthor().getId().toString())
                .authorName(a.getAuthor().getFullName() != null ? a.getAuthor().getFullName() : a.getAuthor().getUsername())
                .authorAvatar("https://ui-avatars.com/api/?name=" + a.getAuthor().getUsername())
                .isInstructor(a.getIsInstructorAnswer())
                .body(a.getBody())
                .isTopAnswer(a.getIsTopAnswer())
                .isInstructorAnswer(a.getIsInstructorAnswer())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
