package sem4.edustreambe.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import sem4.edustreambe.dto.tutor.request.TutorProfileCreationRequest;
import sem4.edustreambe.dto.tutor.request.TutorProfileUpdateRequest;
import sem4.edustreambe.dto.tutor.response.TutorDocumentResponse;
import sem4.edustreambe.dto.tutor.response.TutorProfileResponse;
import sem4.edustreambe.entity.TutorDocument;
import sem4.edustreambe.entity.TutorProfile;

@Mapper(
        componentModel = "spring",
        // Khi update, bỏ qua field null trong request — chỉ update những field được cung cấp
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TutorMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)        // Default DRAFT do @Builder.Default
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "verificationProcesses", ignore = true)
    TutorProfile toTutorProfile(TutorProfileCreationRequest request);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "verificationProcesses", ignore = true)
    void updateTutorProfile(@MappingTarget TutorProfile profile, TutorProfileUpdateRequest request);


    @Mapping(target = "tutorName", source = "user.fullName")
    TutorProfileResponse toTutorProfileResponse(TutorProfile profile);

    TutorDocumentResponse toTutorDocumentResponse(TutorDocument document);
}
