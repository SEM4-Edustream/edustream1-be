package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sem4.edustreambe.dto.quiz.request.QuizQuestionRequest;
import sem4.edustreambe.dto.quiz.request.QuizSubmissionRequest;
import sem4.edustreambe.dto.quiz.response.QuizQuestionResponse;
import sem4.edustreambe.dto.quiz.response.QuizSubmissionResponse;
import sem4.edustreambe.entity.Lesson;
import sem4.edustreambe.entity.QuizAnswerChoice;
import sem4.edustreambe.entity.QuizQuestion;
import sem4.edustreambe.entity.QuizSubmission;
import sem4.edustreambe.entity.User;
import sem4.edustreambe.enums.LessonType;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;
import sem4.edustreambe.mapper.QuizMapper;
import sem4.edustreambe.repository.LessonRepository;
import sem4.edustreambe.repository.QuizAnswerChoiceRepository;
import sem4.edustreambe.repository.QuizQuestionRepository;
import sem4.edustreambe.repository.QuizSubmissionRepository;
import sem4.edustreambe.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuizService {

    QuizQuestionRepository quizQuestionRepository;
    QuizAnswerChoiceRepository quizAnswerChoiceRepository;
    QuizSubmissionRepository quizSubmissionRepository;
    LessonRepository lessonRepository;
    UserRepository userRepository;
    QuizMapper quizMapper;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public List<QuizQuestionResponse> getQuizQuestionsByLesson(String lessonId, boolean isTutor) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        if (lesson.getType() != LessonType.QUIZ) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION); // Could create specific error
        }

        List<QuizQuestion> questions = quizQuestionRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);

        return questions.stream().map(q -> {
            QuizQuestionResponse response = quizMapper.toQuizQuestionResponse(q);
            if (!isTutor) {
                // Hide correct answers from students
                response.getChoices().forEach(choice -> choice.setIsCorrect(null));
            }
            return response;
        }).collect(Collectors.toList());
    }

    public List<QuizQuestionResponse> addQuestionsToQuiz(String lessonId, List<QuizQuestionRequest> requests) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        if (lesson.getType() != LessonType.QUIZ) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        // Delete existing questions for this lesson to replace them
        List<QuizQuestion> existingQuestions = quizQuestionRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);
        quizQuestionRepository.deleteAll(existingQuestions);

        List<QuizQuestion> newQuestions = requests.stream().map(req -> {
            QuizQuestion q = quizMapper.toQuizQuestion(req);
            q.setLesson(lesson);
            
            if (req.getChoices() != null) {
                List<QuizAnswerChoice> choices = req.getChoices().stream().map(choiceReq -> {
                    QuizAnswerChoice c = quizMapper.toQuizAnswerChoice(choiceReq);
                    c.setQuestion(q);
                    return c;
                }).collect(Collectors.toList());
                q.setChoices(choices);
            }
            return q;
        }).collect(Collectors.toList());

        List<QuizQuestion> savedQuestions = quizQuestionRepository.saveAll(newQuestions);

        return savedQuestions.stream()
                .map(quizMapper::toQuizQuestionResponse)
                .collect(Collectors.toList());
    }

    public QuizSubmissionResponse submitQuiz(String lessonId, QuizSubmissionRequest request) {
        User student = getCurrentUser();
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        if (lesson.getType() != LessonType.QUIZ) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        List<QuizQuestion> questions = quizQuestionRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);
        if (questions.isEmpty()) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        int correctAnswers = 0;

        for (QuizQuestion question : questions) {
            List<String> studentSelectedChoiceIds = request.getAnswers().get(question.getId());
            if (studentSelectedChoiceIds == null) {
                studentSelectedChoiceIds = List.of();
            }

            List<String> correctChoiceIds = question.getChoices().stream()
                    .filter(QuizAnswerChoice::getIsCorrect)
                    .map(QuizAnswerChoice::getId)
                    .toList();

            // Check if student selected exactly all correct choices and no incorrect ones
            if (studentSelectedChoiceIds.size() == correctChoiceIds.size() &&
                studentSelectedChoiceIds.containsAll(correctChoiceIds)) {
                correctAnswers++;
            }
        }

        float score = ((float) correctAnswers / questions.size()) * 100;
        boolean passed = score >= 80.0f; // Require 80% to pass

        QuizSubmission submission = quizSubmissionRepository.findByLessonIdAndStudentId(lessonId, student.getId().toString())
                .orElse(QuizSubmission.builder()
                        .lesson(lesson)
                        .student(student)
                        .build());

        submission.setScore(score);
        submission.setPassed(passed);

        quizSubmissionRepository.save(submission);

        return QuizSubmissionResponse.builder()
                .id(submission.getId())
                .score(score)
                .passed(passed)
                .build();
    }
}
