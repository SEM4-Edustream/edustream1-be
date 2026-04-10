package sem4.edustreambe.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import sem4.edustreambe.enums.DocumentType;

@Entity
@Table(name = "tutor_documents")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TutorDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_profile_id", nullable = false)
    TutorProfile tutorProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    DocumentType type;

    @Column(length = 1000, nullable = false)
    String fileUrl;

    @Builder.Default
    Boolean isVerified = false;
}
