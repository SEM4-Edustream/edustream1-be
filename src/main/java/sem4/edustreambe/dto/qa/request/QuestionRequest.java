package sem4.edustreambe.dto.qa.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionRequest {

    @NotBlank(message = "Tiêu đề câu hỏi không được để trống")
    @Size(max = 500, message = "Tiêu đề không được vượt quá 500 ký tự")
    String title;

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    String body;

    // Optional: gắn câu hỏi với bài học cụ thể
    String lessonId;
}
