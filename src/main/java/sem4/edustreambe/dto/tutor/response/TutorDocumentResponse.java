package sem4.edustreambe.dto.tutor.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sem4.edustreambe.enums.DocumentType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TutorDocumentResponse {

    String id;
    DocumentType type;
    String fileUrl;
    Boolean isVerified;
}
