package sem4.edustreambe.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.dto.note.request.NoteRequest;
import sem4.edustreambe.dto.note.response.NoteResponse;
import sem4.edustreambe.service.NoteService;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Notes", description = "Student Notes Management")
public class NoteController {
    NoteService noteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'STUDENT')")
    public ApiResponse<NoteResponse> createNote(@RequestBody @Valid NoteRequest request) {
        return ApiResponse.<NoteResponse>builder()
                .result(noteService.createNote(request))
                .build();
    }

    @GetMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('USER', 'STUDENT')")
    public ApiResponse<List<NoteResponse>> getMyNotesByCourse(@PathVariable String courseId) {
        return ApiResponse.<List<NoteResponse>>builder()
                .result(noteService.getMyNotesByCourse(courseId))
                .build();
    }

    @DeleteMapping("/{noteId}")
    @PreAuthorize("hasAnyRole('USER', 'STUDENT')")
    public ApiResponse<Void> deleteNote(@PathVariable String noteId) {
        noteService.deleteNote(noteId);
        return ApiResponse.<Void>builder().build();
    }
}
