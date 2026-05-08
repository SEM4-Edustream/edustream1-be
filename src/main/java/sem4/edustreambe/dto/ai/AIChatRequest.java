package sem4.edustreambe.dto.ai;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AIChatRequest {
    private String courseId;
    private String message;
}
