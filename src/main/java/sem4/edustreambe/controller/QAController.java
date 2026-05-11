package sem4.edustreambe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.dto.qa.request.AnswerRequest;
import sem4.edustreambe.dto.qa.request.QuestionRequest;
import sem4.edustreambe.dto.qa.response.AnswerResponse;
import sem4.edustreambe.dto.qa.response.QuestionResponse;
import sem4.edustreambe.service.QAService;

@RestController
@RequestMapping("/api/qa")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Q&A API", description = "Hệ thống Hỏi & Đáp giữa học viên và giảng viên")
public class QAController {

    QAService qaService;

    // ─── Tutor: Xem Q&A của các khóa học mình dạy ───────────────────────────
    @GetMapping("/tutor")
    @PreAuthorize("hasRole('TUTOR')")
    @Operation(
        summary = "Tutor xem danh sách Q&A",
        description = "Lấy tất cả câu hỏi từ các khóa học của tutor. Có thể lọc theo courseId và filter (NO_ANSWER, NO_INSTRUCTOR_ANSWER)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ApiResponse<Page<QuestionResponse>> getTutorQuestions(
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String filter,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        return ApiResponse.<Page<QuestionResponse>>builder()
                .result(qaService.getTutorQuestions(courseId, filter, pageable))
                .build();
    }

    // ─── Public/Student: Xem Q&A của một khóa học ───────────────────────────
    @GetMapping("/courses/{courseId}")
    @Operation(summary = "Xem danh sách Q&A của một khóa học")
    public ApiResponse<Page<QuestionResponse>> getCourseQuestions(
            @PathVariable String courseId,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        return ApiResponse.<Page<QuestionResponse>>builder()
                .result(qaService.getCourseQuestions(courseId, pageable))
                .build();
    }

    // ─── Xem chi tiết câu hỏi + câu trả lời ─────────────────────────────────
    @GetMapping("/{questionId}")
    @Operation(summary = "Xem chi tiết một câu hỏi cùng tất cả câu trả lời")
    public ApiResponse<QuestionResponse> getQuestionDetail(@PathVariable String questionId) {
        return ApiResponse.<QuestionResponse>builder()
                .result(qaService.getQuestionDetail(questionId))
                .build();
    }

    // ─── Student: Đặt câu hỏi ────────────────────────────────────────────────
    @PostMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('USER', 'STUDENT', 'TUTOR')")
    @Operation(
        summary = "Đặt câu hỏi trong khóa học (yêu cầu đã enrolled)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ApiResponse<QuestionResponse> createQuestion(
            @PathVariable String courseId,
            @Valid @RequestBody QuestionRequest request) {
        return ApiResponse.<QuestionResponse>builder()
                .result(qaService.createQuestion(courseId, request))
                .message("Câu hỏi đã được đăng thành công!")
                .build();
    }

    // ─── Trả lời câu hỏi ─────────────────────────────────────────────────────
    @PostMapping("/{questionId}/answers")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Trả lời một câu hỏi",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ApiResponse<AnswerResponse> createAnswer(
            @PathVariable String questionId,
            @Valid @RequestBody AnswerRequest request) {
        return ApiResponse.<AnswerResponse>builder()
                .result(qaService.createAnswer(questionId, request))
                .message("Câu trả lời đã được đăng!")
                .build();
    }

    // ─── Tutor: Đánh dấu Top Answer ──────────────────────────────────────────
    @PatchMapping("/answers/{answerId}/top")
    @PreAuthorize("hasRole('TUTOR')")
    @Operation(
        summary = "Tutor đánh dấu/bỏ đánh dấu Top Answer",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ApiResponse<AnswerResponse> markTopAnswer(@PathVariable String answerId) {
        return ApiResponse.<AnswerResponse>builder()
                .result(qaService.markTopAnswer(answerId))
                .build();
    }

    // ─── Resolve câu hỏi ─────────────────────────────────────────────────────
    @PatchMapping("/{questionId}/resolve")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Toggle trạng thái đã giải quyết của câu hỏi",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ApiResponse<QuestionResponse> resolveQuestion(@PathVariable String questionId) {
        return ApiResponse.<QuestionResponse>builder()
                .result(qaService.resolveQuestion(questionId))
                .build();
    }
}
