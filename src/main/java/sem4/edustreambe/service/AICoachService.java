package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sem4.edustreambe.entity.Course;
import sem4.edustreambe.entity.CourseModule;
import sem4.edustreambe.entity.Lesson;
import sem4.edustreambe.enums.LessonType;
import sem4.edustreambe.repository.CourseRepository;
import sem4.edustreambe.repository.LessonRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class AICoachService {

    final CourseRepository courseRepository;
    final LessonRepository lessonRepository;
    final RestTemplate restTemplate;

    @Value("${gemini.api.key:PLACEHOLDER_KEY}")
    String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=}")
    String apiUrl;

    public String chat(String courseId, String userMessage) {
        String cleanApiKey = apiKey.trim();
        if ("PLACEHOLDER_KEY".equals(cleanApiKey)) {
            return "Error: GEMINI_API_KEY is not set. Please check your .env or application.yaml";
        }

        // 1. Thu thập bối cảnh khóa học
        String courseContext = getCourseContext(courseId);

        // 2. Xây dựng Prompt
        String combinedPrompt = String.format(
            "You are EduStream Coach. Here is the course content:\n%s\n\nStudent question: %s\n\nRule: Only answer questions related to the course content above. If unrelated, politely decline.",
            courseContext, userMessage
        );

        // 3. Chuẩn bị Request Body
        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("parts", List.of(Map.of("text", combinedPrompt)));
        contents.add(contentMap);
        requestBody.put("contents", contents);

        // 4. Gọi API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String finalUrl = apiUrl.trim() + cleanApiKey;
        log.info("Calling Gemini at URL: {}", apiUrl.trim() + "***MASKED***"); // Log URL (ẩn Key)

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(finalUrl, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List candidates = (List) response.getBody().get("candidates");
                Map firstCandidate = (Map) candidates.get(0);
                Map content = (Map) firstCandidate.get("content");
                List responseParts = (List) content.get("parts");
                Map firstPart = (Map) responseParts.get(0);
                return (String) firstPart.get("text");
            }
        } catch (Exception e) {
            log.error("Gemini Error: ", e);
            return "Connection Error: " + e.getMessage();
        }

        return "I'm sorry, I couldn't process your request.";
    }

    private String getCourseContext(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        StringBuilder context = new StringBuilder();
        context.append("Course Title: ").append(course.getTitle()).append("\n");
        context.append("Description: ").append(course.getDescription()).append("\n\n");

        for (CourseModule module : course.getModules()) {
            context.append("Module: ").append(module.getTitle()).append("\n");
            List<Lesson> lessons = lessonRepository.findByModuleIdOrderByOrderIndexAsc(module.getId());
            for (Lesson lesson : lessons) {
                if (lesson.getType() == LessonType.TEXT || lesson.getType() == LessonType.ASSIGNMENT) {
                    context.append("- ").append(lesson.getTitle()).append(": ");
                    context.append(lesson.getContent()).append("\n");
                }
            }
        }

        return context.toString();
    }
}
