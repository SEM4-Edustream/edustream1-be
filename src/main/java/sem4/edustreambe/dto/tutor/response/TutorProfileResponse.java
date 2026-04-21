package sem4.edustreambe.dto.tutor.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sem4.edustreambe.enums.VerificationStatus;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TutorProfileResponse {

    String id;
    String tutorName;
    String headline;
    String bio;
    String videoIntroduction;
    VerificationStatus status;
    List<TutorDocumentResponse> documents;

}
