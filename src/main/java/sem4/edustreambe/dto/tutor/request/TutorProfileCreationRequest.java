package sem4.edustreambe.dto.tutor.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TutorProfileCreationRequest {

    @NotBlank(message = "Headline cannot be empty")
    String headline;

    @NotBlank(message = "Bio cannot be empty")
    String bio;

    String videoIntroduction;
}
