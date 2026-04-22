package sem4.edustreambe.dto.tutor.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sem4.edustreambe.enums.VerificationStatus;
import java.time.LocalDateTime;
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
    LocalDateTime verificationStartDate;
    LocalDateTime verifiedAt;
    List<TutorDocumentResponse> documents;

}
