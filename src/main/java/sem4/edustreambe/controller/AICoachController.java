package sem4.edustreambe.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sem4.edustreambe.dto.ai.AIChatRequest;
import sem4.edustreambe.dto.ai.AIChatResponse;
import sem4.edustreambe.service.AICoachService;

@RestController
@RequestMapping("/api/ai/coach")
@RequiredArgsConstructor
public class AICoachController {

    private final AICoachService aiCoachService;

    @PostMapping("/chat")
    public ResponseEntity<AIChatResponse> chat(@RequestBody AIChatRequest request) {
        String answer = aiCoachService.chat(request.getCourseId(), request.getMessage());
        return ResponseEntity.ok(AIChatResponse.builder()
                .answer(answer)
                .build());
    }
}
