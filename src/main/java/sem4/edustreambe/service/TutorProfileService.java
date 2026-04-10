package sem4.edustreambe.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sem4.edustreambe.constant.PredefinedRole;
import sem4.edustreambe.dto.tutor.request.TutorDocumentRequest;
import sem4.edustreambe.dto.tutor.request.TutorProfileCreationRequest;
import sem4.edustreambe.dto.tutor.request.TutorProfileUpdateRequest;
import sem4.edustreambe.dto.tutor.request.VerificationReviewRequest;
import sem4.edustreambe.dto.tutor.response.TutorDocumentResponse;
import sem4.edustreambe.dto.tutor.response.TutorProfileResponse;
import sem4.edustreambe.entity.TutorDocument;
import sem4.edustreambe.entity.TutorProfile;
import sem4.edustreambe.entity.User;
import sem4.edustreambe.entity.VerificationProcess;
import sem4.edustreambe.enums.VerificationStatus;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;
import sem4.edustreambe.mapper.TutorMapper;
import sem4.edustreambe.repository.RoleRepository;
import sem4.edustreambe.repository.TutorDocumentRepository;
import sem4.edustreambe.repository.TutorProfileRepository;
import sem4.edustreambe.repository.UserRepository;
import sem4.edustreambe.repository.VerificationProcessRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TutorProfileService {

    TutorProfileRepository tutorProfileRepository;
    TutorDocumentRepository tutorDocumentRepository;
    VerificationProcessRepository verificationProcessRepository;
    UserRepository userRepository;
    RoleRepository roleRepository;
    TutorMapper tutorMapper;

    // =========================================================
    // HELPER: Lấy User hiện tại từ JWT Security Context
    // =========================================================
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    // =========================================================
    // API DÀNH CHO TUTOR / STUDENT
    // =========================================================

    /**
     * [POST /api/tutor-profiles]
     * Tạo hồ sơ Tutor mới.
     * - Nghiệp vụ: Mỗi User chỉ được có 1 hồ sơ Tutor duy nhất.
     * - Trạng thái khởi tạo: DRAFT.
     */
    public TutorProfileResponse createProfile(TutorProfileCreationRequest request) {
        User currentUser = getCurrentUser();
        log.info("User [{}] is creating a tutor profile", currentUser.getUsername());

        // Kiểm tra đã tồn tại profile chưa (1 User - 1 TutorProfile)
        if (tutorProfileRepository.findByUserId(currentUser.getId()).isPresent()) {
            throw new AppException(ErrorCode.TUTOR_PROFILE_EXISTED);
        }

        TutorProfile profile = tutorMapper.toTutorProfile(request);
        profile.setUser(currentUser);
        // status = DRAFT là mặc định từ @Builder.Default trong entity

        TutorProfile saved = tutorProfileRepository.save(profile);
        log.info("Tutor profile created with id [{}] for user [{}]", saved.getId(), currentUser.getUsername());

        return tutorMapper.toTutorProfileResponse(saved);
    }

    /**
     * [GET /api/tutor-profiles/my-profile]
     * Lấy hồ sơ Tutor của người dùng hiện tại theo JWT context.
     */
    public TutorProfileResponse getMyProfile() {
        User currentUser = getCurrentUser();

        TutorProfile profile = tutorProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.TUTOR_PROFILE_NOT_FOUND));

        return tutorMapper.toTutorProfileResponse(profile);
    }

    /**
     * [PUT /api/tutor-profiles/me]
     * Cập nhật thông tin text của hồ sơ Tutor.
     * - Nghiệp vụ: Chỉ được cập nhật khi profile đang ở trạng thái DRAFT hoặc REJECTED.
     *   PENDING/APPROVED/BANNED không được sửa.
     */
    public TutorProfileResponse updateMyProfile(TutorProfileUpdateRequest request) {
        User currentUser = getCurrentUser();

        TutorProfile profile = tutorProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.TUTOR_PROFILE_NOT_FOUND));

        // Chỉ cho phép sửa khi đang ở DRAFT hoặc REJECTED
        if (profile.getStatus() != VerificationStatus.DRAFT
                && profile.getStatus() != VerificationStatus.REJECTED) {
            log.warn("User [{}] tried to update profile in status [{}]", currentUser.getUsername(), profile.getStatus());
            throw new AppException(ErrorCode.INVALID_PROFILE_STATUS);
        }

        tutorMapper.updateTutorProfile(profile, request);
        TutorProfile saved = tutorProfileRepository.save(profile);

        return tutorMapper.toTutorProfileResponse(saved);
    }

    /**
     * [POST /api/tutor-profiles/documents]
     * Thêm tài liệu chứng minh năng lực (URL S3 do Frontend upload sẵn).
     * - Nghiệp vụ: Chỉ được thêm document khi profile đang ở DRAFT hoặc REJECTED.
     */
    public TutorDocumentResponse addDocument(TutorDocumentRequest request) {
        User currentUser = getCurrentUser();

        TutorProfile profile = tutorProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.TUTOR_PROFILE_NOT_FOUND));

        // Chỉ cho phép thêm document khi DRAFT hoặc REJECTED
        if (profile.getStatus() != VerificationStatus.DRAFT
                && profile.getStatus() != VerificationStatus.REJECTED) {
            throw new AppException(ErrorCode.INVALID_PROFILE_STATUS);
        }

        TutorDocument document = TutorDocument.builder()
                .tutorProfile(profile)
                .type(request.getType())
                .fileUrl(request.getFileUrl())
                .isVerified(false)
                .build();

        TutorDocument saved = tutorDocumentRepository.save(document);
        log.info("Document [{}] added to profile [{}]", saved.getId(), profile.getId());

        return tutorMapper.toTutorDocumentResponse(saved);
    }

    /**
     * [POST /api/tutor-profiles/submit-verification]
     * Tutor xác nhận đã chuẩn bị đủ giấy tờ và nộp hồ sơ để Admin duyệt.
     * - Nghiệp vụ:
     *   1. Chỉ được submit từ trạng thái DRAFT hoặc REJECTED.
     *   2. Phải có ít nhất 1 tài liệu đã được đính kèm.
     *   3. Sau khi submit, trạng thái chuyển sang PENDING.
     */
    public TutorProfileResponse submitForVerification() {
        try {
            User currentUser = getCurrentUser();
            log.info("User [{}] is submitting profile for verification", currentUser.getUsername());

            TutorProfile profile = tutorProfileRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.TUTOR_PROFILE_NOT_FOUND));

            if (profile.getStatus() != VerificationStatus.DRAFT
                    && profile.getStatus() != VerificationStatus.REJECTED) {
                throw new AppException(ErrorCode.INVALID_PROFILE_STATUS);
            }

            long documentCount = tutorDocumentRepository.countByTutorProfileId(profile.getId());
            if (documentCount == 0) {
                throw new AppException(ErrorCode.PROFILE_MUST_HAVE_DOCUMENT);
            }

            profile.setStatus(VerificationStatus.PENDING);
            TutorProfile saved = tutorProfileRepository.save(profile);
            log.info("Profile [{}] status changed to PENDING", saved.getId());

            return tutorMapper.toTutorProfileResponse(saved);
        } catch (AppException ae) {
            throw ae;
        } catch (Exception e) {
            log.error("Internal Server Error in submitForVerification: ", e);
            throw new RuntimeException("Lỗi ẩn 500: " + e.toString() + " | Nguyên nhân: " + (e.getCause() != null ? e.getCause().toString() : "N/A"));
        }
    }

    // =========================================================
    // API DÀNH CHO ADMIN
    // =========================================================

    /**
     * [GET /api/admin/tutor-profiles?status=PENDING]
     * Lấy danh sách hồ sơ Tutor theo filter status.
     * Mặc định trả về danh sách PENDING nếu không truyền status.
     */
    public List<TutorProfileResponse> getProfilesByStatus(VerificationStatus status) {
        VerificationStatus filterStatus = (status != null) ? status : VerificationStatus.PENDING;
        log.info("Admin fetching profiles with status [{}]", filterStatus);

        return tutorProfileRepository.findAllByStatus(filterStatus).stream()
                .map(tutorMapper::toTutorProfileResponse)
                .toList();
    }

    /**
     * [GET /api/admin/tutor-profiles/{id}]
     * Admin xem chi tiết hồ sơ Tutor (full info + documents + lịch sử duyệt).
     */
    public TutorProfileResponse getProfileById(String profileId) {
        TutorProfile profile = tutorProfileRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.TUTOR_PROFILE_NOT_FOUND));

        return tutorMapper.toTutorProfileResponse(profile);
    }

    /**
     * [POST /api/admin/tutor-profiles/{id}/verify]
     * Admin duyệt hoặc từ chối hồ sơ Tutor.
     * - Nghiệp vụ APPROVE:
     *   1. Chỉ được APPROVE hồ sơ đang ở trạng thái PENDING.
     *   2. Đổi trạng thái profile sang APPROVED.
     *   3. Tự động đổi Role của User sang TUTOR.
     *   4. Ghi lại lịch sử vào bảng verification_processes.
     * - Nghiệp vụ REJECT:
     *   1. Chỉ được REJECT hồ sơ đang ở trạng thái PENDING.
     *   2. Đổi trạng thái profile sang REJECTED (Tutor có thể nộp lại).
     *   3. Ghi lại lịch sử kèm lý do từ chối (reviewComment).
     */
    public TutorProfileResponse reviewProfile(String profileId, VerificationReviewRequest request) {
        // Chỉ chấp nhận action APPROVED hoặc REJECTED
        if (request.getAction() != VerificationStatus.APPROVED
                && request.getAction() != VerificationStatus.REJECTED) {
            throw new AppException(ErrorCode.INVALID_PROFILE_STATUS);
        }

        TutorProfile profile = tutorProfileRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.TUTOR_PROFILE_NOT_FOUND));

        // Chỉ được duyệt hồ sơ đang PENDING
        if (profile.getStatus() != VerificationStatus.PENDING) {
            log.warn("Admin tried to review profile [{}] with status [{}] (must be PENDING)",
                    profileId, profile.getStatus());
            throw new AppException(ErrorCode.INVALID_PROFILE_STATUS);
        }

        // Lấy thông tin Admin đang thao tác từ Security Context
        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User adminUser = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        VerificationStatus previousStatus = profile.getStatus();

        // Ghi lịch sử duyệt hồ sơ
        VerificationProcess verificationProcess = VerificationProcess.builder()
                .tutorProfile(profile)
                .adminId(adminUser.getId().toString())
                .previousStatus(previousStatus)
                .newStatus(request.getAction())
                .reviewComment(request.getReviewComment())
                .build();
        verificationProcessRepository.save(verificationProcess);

        // Xử lý theo action
        profile.setStatus(request.getAction());

        if (request.getAction() == VerificationStatus.APPROVED) {
            log.info("Admin [{}] APPROVED profile [{}] — assigning TUTOR role", adminUsername, profileId);

            // Đổi role User sang TUTOR
            var tutorRole = roleRepository.findByName(PredefinedRole.TUTOR_ROLE)
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

            User profileOwner = profile.getUser();
            profileOwner.setRole(tutorRole);
            userRepository.save(profileOwner);

        } else {
            log.info("Admin [{}] REJECTED profile [{}] with comment: [{}]",
                    adminUsername, profileId, request.getReviewComment());
        }

        TutorProfile saved = tutorProfileRepository.save(profile);
        return tutorMapper.toTutorProfileResponse(saved);
    }
}
