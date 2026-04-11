package com.aistudy.api.signup.service;

import com.aistudy.api.admin.ClassroomRepository;
import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.auth.AuthUserEntity;
import com.aistudy.api.auth.AuthUserRepository;
import com.aistudy.api.auth.Role;
import com.aistudy.api.common.BadRequestException;
import com.aistudy.api.common.ForbiddenException;
import com.aistudy.api.common.NotFoundException;
import com.aistudy.api.signup.dto.CreateStudentSignupRequest;
import com.aistudy.api.signup.dto.CreateTeacherSignupRequest;
import com.aistudy.api.signup.model.ApprovalAuditLogEntity;
import com.aistudy.api.signup.model.SchoolMasterEntity;
import com.aistudy.api.signup.model.SignupRequestEntity;
import com.aistudy.api.signup.model.SignupRole;
import com.aistudy.api.signup.model.SignupStatus;
import com.aistudy.api.signup.repository.ApprovalAuditLogRepository;
import com.aistudy.api.signup.repository.SchoolMasterRepository;
import com.aistudy.api.signup.repository.SchoolOperatorMembershipRepository;
import com.aistudy.api.signup.repository.SignupRequestRepository;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignupService {
	private final SchoolMasterRepository schoolMasterRepository;
	private final SignupRequestRepository signupRequestRepository;
	private final SchoolOperatorMembershipRepository schoolOperatorMembershipRepository;
	private final ApprovalAuditLogRepository approvalAuditLogRepository;
	private final AuthUserRepository authUserRepository;
	private final ClassroomRepository classroomRepository;
	private final PasswordEncoder passwordEncoder;

	public SignupService(SchoolMasterRepository schoolMasterRepository, SignupRequestRepository signupRequestRepository, SchoolOperatorMembershipRepository schoolOperatorMembershipRepository, ApprovalAuditLogRepository approvalAuditLogRepository, AuthUserRepository authUserRepository, ClassroomRepository classroomRepository, PasswordEncoder passwordEncoder) {
		this.schoolMasterRepository = schoolMasterRepository;
		this.signupRequestRepository = signupRequestRepository;
		this.schoolOperatorMembershipRepository = schoolOperatorMembershipRepository;
		this.approvalAuditLogRepository = approvalAuditLogRepository;
		this.authUserRepository = authUserRepository;
		this.classroomRepository = classroomRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public List<SchoolMasterEntity> searchSchools(String keyword) {
		return schoolMasterRepository.findTop20ByNameContainingIgnoreCaseOrderByNameAsc(keyword == null ? "" : keyword);
	}

	/** 교사 가입 요청 — 학교 활성 여부를 검증합니다. */
	@Transactional
	public SignupRequestEntity requestTeacherSignup(CreateTeacherSignupRequest request) {
		SchoolMasterEntity school = schoolMasterRepository.findById(request.schoolId()).orElseThrow(() -> new NotFoundException("학교를 찾을 수 없습니다."));
		if (!school.isActive()) throw new BadRequestException("비활성 학교입니다.");
		if (authUserRepository.findByLoginId(request.loginId()).isPresent()) throw new BadRequestException("이미 사용 중인 로그인 ID입니다.");
		return signupRequestRepository.save(new SignupRequestEntity(
			request.schoolId(),
			null,
			request.displayName(),
			request.loginId(),
			passwordEncoder.encode(request.password()),
			SignupRole.TEACHER,
			request.schoolEmail(),
			null,
			null,
			request.consentTerms(),
			request.consentPrivacy(),
			request.consentStudentNotice()
		));
	}

	/** 학생 가입 요청 — 학교 활성 여부 및 학급-학교 소속 일치를 검증합니다. PIN을 해시하여 저장합니다.
	 *  studentCode는 선택 사항이며, 미제공 시 운영자가 승인 시 지정합니다.
	 *  동일 실명은 허용되며, 고유성은 studentCode 범위로 보장합니다.
	 */
	@Transactional
	public SignupRequestEntity requestStudentSignup(CreateStudentSignupRequest request) {
		SchoolMasterEntity school = schoolMasterRepository.findById(request.schoolId()).orElseThrow(() -> new NotFoundException("학교를 찾을 수 없습니다."));
		if (!school.isActive()) throw new BadRequestException("비활성 학교입니다.");
		// studentCode가 제공된 경우 학교 범위 내 고유성 검증
		if (request.studentCode() != null && !request.studentCode().isBlank()) {
			if (authUserRepository.existsBySchoolIdAndStudentCodeAndRole(request.schoolId(), request.studentCode(), Role.STUDENT)) {
				throw new BadRequestException("같은 학교에 동일한 학생 코드가 이미 존재합니다.");
			}
		}
		if (request.classroomId() != null && !request.classroomId().isBlank()) {
			classroomRepository.findByIdAndSchoolId(request.classroomId(), request.schoolId())
				.orElseThrow(() -> new NotFoundException("해당 학교의 학급을 찾을 수 없습니다."));
		}
		return signupRequestRepository.save(new SignupRequestEntity(
			request.schoolId(),
			request.classroomId(),
			request.realName(),
			null,
			passwordEncoder.encode(request.pin()),  // PIN 해시를 passwordHash 필드에 저장
			SignupRole.STUDENT,
			null,
			request.realName(),
			request.studentCode(),
			request.consentTerms(),
			request.consentPrivacy(),
			request.consentStudentNotice()
		));
	}

	@Transactional(readOnly = true)
	public List<SignupRequestEntity> pendingRequests(AuthUser reviewer, String schoolId) {
		ensureSchoolOperator(reviewer, schoolId);
		return signupRequestRepository.findBySchoolIdAndStatusOrderByCreatedAtDesc(schoolId, SignupStatus.PENDING);
	}

	/** 가입 요청 승인/반려 — 운영자는 자신이 관리하는 학교의 요청만 처리할 수 있습니다.
	 *  학생 승인 시 studentCode를 확인/지정합니다.
	 */
	@Transactional
	public SignupRequestEntity review(AuthUser reviewer, String signupRequestId, boolean approve, String rejectionReason, String operatorStudentCode) {
		SignupRequestEntity request = signupRequestRepository.findById(signupRequestId).orElseThrow(() -> new NotFoundException("가입 요청을 찾을 수 없습니다."));
		ensureSchoolOperator(reviewer, request.getSchoolId());
		if (request.getStatus() != SignupStatus.PENDING) throw new BadRequestException("이미 처리된 가입 요청입니다.");
		if (approve) {
			if (request.getRole() == SignupRole.STUDENT) {
				// 학생 승인: studentCode 확정 (운영자 지정 > 요청 시 제출 > 자동 생성)
				String studentCode = resolveStudentCode(request, operatorStudentCode);
				if (authUserRepository.existsBySchoolIdAndStudentCodeAndRole(request.getSchoolId(), studentCode, Role.STUDENT)) {
					throw new BadRequestException("같은 학교에 동일한 학생 코드가 이미 존재합니다.");
				}
				request.setStudentCode(studentCode);
				// 학생 승인: PIN 기반 인증, 임시 비밀번호 생성 없음
				String provisionedLoginId = "student-" + request.getId().substring(0, 8);
				String placeholderPassword = passwordEncoder.encode(java.util.UUID.randomUUID().toString());
				AuthUserEntity user = new AuthUserEntity(
					request.getSchoolId(),
					request.getClassroomId(),
					provisionedLoginId,
					placeholderPassword,
					request.getRequesterName(),
					Role.STUDENT,
					request.getPasswordHash(),  // PIN 해시를 pin 필드로 이관
					studentCode
				);
				authUserRepository.save(user);
				request.approve(reviewer.userId(), provisionedLoginId);  // 임시 비밀번호 없음
			} else {
				// 교사/운영자 승인: 기존 비밀번호 기반 인증 유지
				String provisionedLoginId = request.getLoginId();
				if (authUserRepository.findByLoginId(provisionedLoginId).isPresent()) {
					throw new BadRequestException("이미 사용 중인 로그인 ID입니다.");
				}
				AuthUserEntity user = new AuthUserEntity(
					request.getSchoolId(),
					request.getClassroomId(),
					provisionedLoginId,
					request.getPasswordHash(),
					request.getRequesterName(),
					Role.TEACHER
				);
				authUserRepository.save(user);
				request.approve(reviewer.userId(), provisionedLoginId, null);
			}
			approvalAuditLogRepository.save(new ApprovalAuditLogEntity(request.getSchoolId(), request.getId(), reviewer.userId(), "APPROVED", null));
		} else {
			request.reject(reviewer.userId(), rejectionReason);
			approvalAuditLogRepository.save(new ApprovalAuditLogEntity(request.getSchoolId(), request.getId(), reviewer.userId(), "REJECTED", rejectionReason));
		}
		return signupRequestRepository.save(request);
	}

	/** studentCode 확정 로직: 운영자 지정 > 요청 시 제출 > 자동 생성 */
	private String resolveStudentCode(SignupRequestEntity request, String operatorStudentCode) {
		if (operatorStudentCode != null && !operatorStudentCode.isBlank()) {
			return operatorStudentCode.trim();
		}
		if (request.getStudentCode() != null && !request.getStudentCode().isBlank()) {
			return request.getStudentCode();
		}
		return generateStudentCode();
	}

	/** 학교 범위 내 고유한 학생 코드를 자동 생성합니다. */
	private String generateStudentCode() {
		for (int attempt = 0; attempt < 10; attempt++) {
			String code = "S" + String.format("%04d", ThreadLocalRandom.current().nextInt(1, 10000));
			// 학교 범위 검증은 호출 측에서 수행하므로 여기서는 형식만 보장
			return code;
		}
		throw new IllegalStateException("학생 코드를 생성할 수 없습니다.");
	}

	private void ensureSchoolOperator(AuthUser reviewer, String schoolId) {
		if (reviewer.role() != Role.OPERATOR) {
			throw new ForbiddenException("학교 운영자만 가입 요청을 검토할 수 있습니다.");
		}
		schoolOperatorMembershipRepository.findBySchoolIdAndUserIdAndActiveTrue(schoolId, reviewer.userId())
			.orElseThrow(() -> new ForbiddenException("학교 운영자 권한이 없습니다."));
	}
}