package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sem4.edustreambe.dto.note.request.NoteRequest;
import sem4.edustreambe.dto.note.response.NoteResponse;
import sem4.edustreambe.entity.Course;
import sem4.edustreambe.entity.Lesson;
import sem4.edustreambe.entity.Note;
import sem4.edustreambe.entity.User;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;
import sem4.edustreambe.repository.CourseRepository;
import sem4.edustreambe.repository.LessonRepository;
import sem4.edustreambe.repository.NoteRepository;
import sem4.edustreambe.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NoteService {
    NoteRepository noteRepository;
    UserRepository userRepository;
    CourseRepository courseRepository;
    LessonRepository lessonRepository;

    public NoteResponse createNote(NoteRequest request) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsernameOrEmail(login)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

        Note note = Note.builder()
                .user(user)
                .course(course)
                .lesson(lesson)
                .content(request.getContent())
                .timestampSeconds(request.getTimestampSeconds())
                .build();

        note = noteRepository.save(note);

        return mapToResponse(note);
    }

    public List<NoteResponse> getMyNotesByCourse(String courseId) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsernameOrEmail(login)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return noteRepository.findAllByUserIdAndCourseIdOrderByCreatedAtDesc(user.getId(), courseId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteNote(String noteId) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsernameOrEmail(login)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTE_NOT_FOUND));

        if (!note.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        noteRepository.delete(note);
    }

    private NoteResponse mapToResponse(Note note) {
        return NoteResponse.builder()
                .id(note.getId())
                .lessonId(note.getLesson().getId())
                .lessonTitle(note.getLesson().getTitle())
                .content(note.getContent())
                .timestampSeconds(note.getTimestampSeconds())
                .createdDate(note.getCreatedAt())
                .build();
    }
}
