package sem4.edustreambe.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import sem4.edustreambe.enums.VerificationStatus;

@Entity
@Table(name = "verification_processes")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerificationProcess extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_profile_id", nullable = false)
    TutorProfile tutorProfile;

    @Column(nullable = false)
    String adminId; // UUID of the admin who processed this

    @Enumerated(EnumType.STRING)
    VerificationStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    VerificationStatus newStatus;

    @Column(columnDefinition = "TEXT")
    String reviewComment;
}
