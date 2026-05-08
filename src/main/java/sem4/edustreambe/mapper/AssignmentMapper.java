package sem4.edustreambe.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sem4.edustreambe.dto.assignment.response.AssignmentSubmissionResponse;
import sem4.edustreambe.entity.AssignmentSubmission;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {UserMapper.class}
)
public interface AssignmentMapper {

    @Mapping(target = "student", source = "student")
    AssignmentSubmissionResponse toAssignmentSubmissionResponse(AssignmentSubmission submission);
}
