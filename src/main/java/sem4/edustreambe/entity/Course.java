package sem4.edustreambe.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import sem4.edustreambe.enums.CourseStatus;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Course extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_profile_id", nullable = false)
    TutorProfile tutorProfile;

    @Column(nullable = false, length = 255)
    String title;

    @Column(length = 255)
    String subtitle;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(length = 50)
    @Builder.Default
    String language = "English (US)";

    @Column(length = 50)
    String level;

    @Column(length = 100)
    String category;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "course_learning_objectives", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "objective", length = 500)
    List<String> learningObjectives;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "course_prerequisites", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "prerequisite", length = 500)
    List<String> prerequisites;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "course_target_audiences", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "audience", length = 500)
    List<String> targetAudiences;

    @Column(length = 1000)
    String thumbnailUrl;

    @Column(precision = 12, scale = 2)
    BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    CourseStatus status = CourseStatus.DRAFT;

    @Column(name = "average_rating")
    @Builder.Default
    Float averageRating = 0.0f;

    @Column(name = "review_count")
    @Builder.Default
    Integer reviewCount = 0;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    List<CourseModule> modules;
}
