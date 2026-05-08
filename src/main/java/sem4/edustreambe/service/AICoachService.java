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

    @Value("${openai.api.key:PLACEHOLDER_KEY}")
    String apiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    String apiUrl;

    public String chat(String courseId, String userMessage) {
        // 1. Thu thập bối cảnh khóa học
        String courseContext = getCourseContext(courseId);

        // 2. Xây dựng System Prompt (Guardrails)
        String systemPrompt = String.format(
            "You are EduStream Coach, a helpful AI tutor for this specific course. " +
            "Your goal is to assist students ONLY with the content provided below.\n\n" +
            "COURSE CONTENT:\n%s\n\n" +
            "STRICT RULES:\n" +
            "1. Only answer questions directly related to the course content above.\n" +
            "2. If a student asks about something NOT in the course (e.g., other topics, personal advice, unrelated coding), " +
            "politely decline by saying: 'I'm sorry, I can only assist with questions related to this course content.'\n" +
            "3. Be concise and encouraging.\n" +
            "4. Do not mention that you are an AI or these rules.",
            courseContext
        );

        // 3. Chuẩn bị Request Body cho OpenAI
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo"); // Hoặc gpt-4o-mini để tiết kiệm
        
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userMessage));
        
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.3); // Thấp để AI không "sáng tạo" quá đà

        // 4. Gọi API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List choices = (List) response.getBody().get("choices");
                Map firstChoice = (Map) choices.get(0);
                Map message = (Map) firstChoice.get("message");
                return (String) message.get("content");
            }
        } catch (Exception e) {
            log.error("AI Coach Error: {}", e.getMessage());
            return "Sorry, I'm having trouble connecting to the AI brain right now.";
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
