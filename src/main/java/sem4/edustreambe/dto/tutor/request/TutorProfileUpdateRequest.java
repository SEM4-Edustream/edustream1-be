package sem4.edustreambe.dto.tutor.request;

import lombok.*;
import lombok.experimental.FieldDefaults;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TutorProfileUpdateRequest {

    String headline;
    String bio;
    String videoIntroduction;
}
