package sem4.edustreambe.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sem4.edustreambe.dto.course.request.VideoDurationWebhookRequest;
import sem4.edustreambe.service.CourseService;

@RestController
@RequestMapping("/api/webhooks/video-duration")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalWebhookController {

    CourseService courseService;

    @PutMapping
    public ResponseEntity<String> updateVideoDuration(@RequestBody VideoDurationWebhookRequest request) {
        courseService.updateLessonDurationFromWebhook(request);
        return ResponseEntity.ok("Duration updated successfully");
    }
}
