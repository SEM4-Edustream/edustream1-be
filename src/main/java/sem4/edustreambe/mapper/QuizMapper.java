package sem4.edustreambe.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sem4.edustreambe.dto.quiz.request.QuizAnswerChoiceRequest;
import sem4.edustreambe.dto.quiz.request.QuizQuestionRequest;
import sem4.edustreambe.dto.quiz.response.QuizAnswerChoiceResponse;
import sem4.edustreambe.dto.quiz.response.QuizQuestionResponse;
import sem4.edustreambe.entity.QuizAnswerChoice;
import sem4.edustreambe.entity.QuizQuestion;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface QuizMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "question", ignore = true)
    QuizAnswerChoice toQuizAnswerChoice(QuizAnswerChoiceRequest request);

    QuizAnswerChoiceResponse toQuizAnswerChoiceResponse(QuizAnswerChoice choice);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "choices", ignore = true)
    QuizQuestion toQuizQuestion(QuizQuestionRequest request);

    QuizQuestionResponse toQuizQuestionResponse(QuizQuestion question);
}
