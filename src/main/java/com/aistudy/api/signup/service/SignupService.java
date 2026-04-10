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

	@Transactional
	public SignupRequestEntity requestTeacherSignup(CreateTeacherSignupRequest request) {
		SchoolMasterEntity school = schoolMasterRepository.findById(request.schoolId()).orElseThrow(() -> new NotFoundException("학교를 찾을 수 없습니다."));
		if (!school.isActive()) throw new BadRequestException("비활성 학교입니다.");
		if (school.getEmailDomain() != null && !request.schoolEmail().endsWith("@" + school.getEmailDomain())) {
			throw new BadRequestException("학교 이메일 도메인이 일치하지 않습니다.");
		}
		return signupRequestRepository.save(new SignupRequestEntity(
			request.schoolId(),
			null,
			request.displayName(),
			request.loginId(),
			passwordEncoder.encode(request.password()),
			SignupRole.TEACHER,
			request.schoolEmail(),
			null,
			request.consentTerms(),
			request.consentPrivacy(),
			request.consentStudentNotice()
		));
	}

	@Transactional
	public SignupRequestEntity requestStudentSignup(CreateStudentSignupRequest request) {
		schoolMasterRepository.findById(request.schoolId()).orElseThrow(() -> new NotFoundException("학교를 찾을 수 없습니다."));
		if (request.classroomId() != null && !request.classroomId().isBlank()) {
			classroomRepository.findById(request.classroomId()).orElseThrow(() -> new NotFoundException("학급을 찾을 수 없습니다."));
		}
		return signupRequestRepository.save(new SignupRequestEntity(
			request.schoolId(),
			request.classroomId(),
			request.realName(),
			null,
			null,
			SignupRole.STUDENT,
			null,
			request.realName(),
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

	@Transactional
	public SignupRequestEntity review(AuthUser reviewer, String signupRequestId, boolean approve, String rejectionReason) {
		SignupRequestEntity request = signupRequestRepository.findById(signupRequestId).orElseThrow(() -> new NotFoundException("가입 요청을 찾을 수 없습니다."));
		ensureSchoolOperator(reviewer, request.getSchoolId());
		if (request.getStatus() != SignupStatus.PENDING) throw new BadRequestException("이미 처리된 가입 요청입니다.");
		if (approve) {
			String provisionedLoginId = request.getLoginId() == null ? "student-" + request.getId().substring(0, 8) : request.getLoginId();
			String provisionedTempPassword = request.getPasswordHash() == null ? "student123" : null;
			AuthUserEntity user = new AuthUserEntity(
				request.getSchoolId(),
				request.getClassroomId(),
				provisionedLoginId,
				request.getPasswordHash() == null ? passwordEncoder.encode(provisionedTempPassword) : request.getPasswordHash(),
				request.getRequesterName(),
				request.getRole() == SignupRole.TEACHER ? Role.TEACHER : Role.STUDENT
			);
			authUserRepository.save(user);
			request.approve(reviewer.userId(), provisionedLoginId, provisionedTempPassword);
			approvalAuditLogRepository.save(new ApprovalAuditLogEntity(reviewer.schoolId(), request.getId(), reviewer.userId(), "APPROVED", null));
		} else {
			request.reject(reviewer.userId(), rejectionReason);
			approvalAuditLogRepository.save(new ApprovalAuditLogEntity(reviewer.schoolId(), request.getId(), reviewer.userId(), "REJECTED", rejectionReason));
		}
		return signupRequestRepository.save(request);
	}

	private void ensureSchoolOperator(AuthUser reviewer, String schoolId) {
		if (reviewer.role() != Role.OPERATOR) {
			throw new ForbiddenException("학교 운영자만 가입 요청을 검토할 수 있습니다.");
		}
		schoolOperatorMembershipRepository.findBySchoolIdAndUserIdAndActiveTrue(schoolId, reviewer.userId())
			.orElseThrow(() -> new ForbiddenException("학교 운영자 권한이 없습니다."));
	}
}
