package sem4.edustreambe.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import sem4.edustreambe.enums.VerificationStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tutor_profiles")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TutorProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    User user;

    @Column(length = 255)
    String headline;

    @Column(columnDefinition = "TEXT")
    String bio;

    @Column(length = 500)
    String videoIntroduction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    VerificationStatus status = VerificationStatus.DRAFT;

    LocalDateTime verificationStartDate;
    LocalDateTime verifiedAt;

    @OneToMany(mappedBy = "tutorProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    List<TutorDocument> documents;

    @OneToMany(mappedBy = "tutorProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    List<VerificationProcess> verificationProcesses;
}
