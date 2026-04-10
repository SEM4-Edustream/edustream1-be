package sem4.edustreambe.dto.tutor.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import sem4.edustreambe.enums.DocumentType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TutorDocumentRequest {

    @NotNull(message = "Document type is required")
    DocumentType type;

    @NotBlank(message = "File URL cannot be empty")
    String fileUrl;
}
