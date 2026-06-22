package sem4.edustreambe.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import sem4.edustreambe.enums.CourseStatus;
import sem4.edustreambe.enums.CourseLevel;

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

    @Column(length = 1000)
    String thumbnailUrl;

    @Column(precision = 12, scale = 2)
    BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    CourseLevel level;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    Category category;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    List<CourseModule> modules;

    @ElementCollection
    @CollectionTable(name = "course_learning_objectives", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "objective")
    List<String> learningObjectives;

    @ElementCollection
    @CollectionTable(name = "course_prerequisites", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "prerequisite")
    List<String> prerequisites;

    @ElementCollection
    @CollectionTable(name = "course_target_audiences", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "audience")
    List<String> targetAudiences;
}
