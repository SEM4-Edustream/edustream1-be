package sem4.edustreambe.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import sem4.edustreambe.enums.AssignmentStatus;

@Entity
@Table(name = "assignment_submissions")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignmentSubmission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    User student;

    @Column(columnDefinition = "TEXT")
    String content;

    @Column(length = 1000)
    String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    AssignmentStatus status = AssignmentStatus.SUBMITTED;

    @Column
    Float grade;

    @Column(columnDefinition = "TEXT")
    String feedback;
}
