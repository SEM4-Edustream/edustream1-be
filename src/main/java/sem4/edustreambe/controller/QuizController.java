package sem4.edustreambe.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.dto.quiz.request.QuizQuestionRequest;
import sem4.edustreambe.dto.quiz.request.QuizSubmissionRequest;
import sem4.edustreambe.dto.quiz.response.QuizQuestionResponse;
import sem4.edustreambe.dto.quiz.response.QuizSubmissionResponse;
import sem4.edustreambe.service.QuizService;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuizController {

    QuizService quizService;

    // --- Tutor Endpoints ---
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    @PostMapping("/{lessonId}/questions")
    public ApiResponse<List<QuizQuestionResponse>> addQuestionsToQuiz(
            @PathVariable String lessonId,
            @RequestBody @Valid List<QuizQuestionRequest> requests) {
        return ApiResponse.<List<QuizQuestionResponse>>builder()
                .result(quizService.addQuestionsToQuiz(lessonId, requests))
                .build();
    }

    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    @GetMapping("/{lessonId}/questions/tutor")
    public ApiResponse<List<QuizQuestionResponse>> getQuizQuestionsForTutor(@PathVariable String lessonId) {
        return ApiResponse.<List<QuizQuestionResponse>>builder()
                .result(quizService.getQuizQuestionsByLesson(lessonId, true))
                .build();
    }

    // --- Student Endpoints ---
    @GetMapping("/{lessonId}/questions")
    public ApiResponse<List<QuizQuestionResponse>> getQuizQuestionsForStudent(@PathVariable String lessonId) {
        return ApiResponse.<List<QuizQuestionResponse>>builder()
                .result(quizService.getQuizQuestionsByLesson(lessonId, false))
                .build();
    }

    @PostMapping("/{lessonId}/submit")
    public ApiResponse<QuizSubmissionResponse> submitQuiz(
            @PathVariable String lessonId,
            @RequestBody @Valid QuizSubmissionRequest request) {
        return ApiResponse.<QuizSubmissionResponse>builder()
                .result(quizService.submitQuiz(lessonId, request))
                .build();
    }
}
