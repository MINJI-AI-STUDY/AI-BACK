package com.aistudy.api.admin;

import com.aistudy.api.admin.dto.AdminUserResponse;
import com.aistudy.api.admin.dto.ClassroomResponse;
import com.aistudy.api.admin.dto.CreateAdminUserRequest;
import com.aistudy.api.admin.dto.CreateClassroomRequest;
import com.aistudy.api.admin.dto.CreateSchoolRequest;
import com.aistudy.api.admin.dto.SchoolResponse;
import com.aistudy.api.admin.dto.UpdateAdminUserRequest;
import com.aistudy.api.admin.dto.UpdateClassroomRequest;
import com.aistudy.api.admin.dto.UpdateSchoolRequest;
import com.aistudy.api.auth.AuthService;
import com.aistudy.api.auth.AuthUser;
import com.aistudy.api.auth.AuthUserEntity;
import com.aistudy.api.auth.AuthUserRepository;
import com.aistudy.api.auth.Role;
import com.aistudy.api.channel.dto.CurateTestChannelsRequest;
import com.aistudy.api.channel.dto.CurateTestChannelsResponse;
import com.aistudy.api.channel.service.ChannelService;
import com.aistudy.api.common.ForbiddenException;
import com.aistudy.api.common.NotFoundException;
import com.aistudy.api.signup.dto.SchoolMasterSyncResponse;
import com.aistudy.api.signup.model.SchoolMasterEntity;
import com.aistudy.api.signup.model.SchoolOperatorMembershipEntity;
import com.aistudy.api.signup.repository.SchoolMasterRepository;
import com.aistudy.api.signup.repository.SchoolOperatorMembershipRepository;
import com.aistudy.api.signup.service.SchoolMasterSyncService;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 운영자 디렉토리 관리 — MVP: 운영자는 소속 학교 범위 내에서만 관리합니다. */
@RestController
@RequestMapping("/api/operator")
public class AdminDirectoryController {
	private final AuthService authService;
	private final SchoolRepository schoolRepository;
	private final ClassroomRepository classroomRepository;
	private final AuthUserRepository authUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final ChannelService channelService;
	private final SchoolMasterSyncService schoolMasterSyncService;
	private final SchoolMasterRepository schoolMasterRepository;
	private final SchoolOperatorMembershipRepository schoolOperatorMembershipRepository;

	public AdminDirectoryController(AuthService authService, SchoolRepository schoolRepository, ClassroomRepository classroomRepository, AuthUserRepository authUserRepository, PasswordEncoder passwordEncoder, ChannelService channelService, SchoolMasterSyncService schoolMasterSyncService, SchoolMasterRepository schoolMasterRepository, SchoolOperatorMembershipRepository schoolOperatorMembershipRepository) {
		this.authService = authService;
		this.schoolRepository = schoolRepository;
		this.classroomRepository = classroomRepository;
		this.authUserRepository = authUserRepository;
		this.passwordEncoder = passwordEncoder;
		this.channelService = channelService;
		this.schoolMasterSyncService = schoolMasterSyncService;
		this.schoolMasterRepository = schoolMasterRepository;
		this.schoolOperatorMembershipRepository = schoolOperatorMembershipRepository;
	}

	/** 운영자가 관리하는 학교 목록을 반환합니다. */
	@GetMapping("/schools")
	public List<SchoolResponse> getSchools(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
		AuthUser operator = authService.requireRole(authorizationHeader, Role.OPERATOR);
		List<String> managedSchoolIds = getManagedSchoolIds(operator);
		List<SchoolResponse> schools = schoolRepository.findAllById(managedSchoolIds).stream().map(SchoolResponse::from).toList();
		if (!schools.isEmpty() || managedSchoolIds.isEmpty()) {
			return schools;
		}
		return schoolMasterRepository.findAllById(managedSchoolIds).stream().map(this::toSchoolResponse).toList();
	}

	/** 학교 생성 — 초기 설정 시 필요하며, 운영자 소속 학교로 등록됩니다. */
	@PostMapping("/schools")
	public SchoolResponse createSchool(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @Valid @RequestBody CreateSchoolRequest request) {
		AuthUser operator = authService.requireRole(authorizationHeader, Role.OPERATOR);
		School school = schoolRepository.save(new School(request.name()));
		schoolOperatorMembershipRepository.save(new SchoolOperatorMembershipEntity(school.getId(), operator.userId()));
		return SchoolResponse.from(school);
	}

	/** 학교 정보 수정 — 운영자는 자신이 관리하는 학교만 수정할 수 있습니다. */
	@PatchMapping("/schools/{schoolId}")
	public SchoolResponse updateSchool(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String schoolId, @Valid @RequestBody UpdateSchoolRequest request) {
		AuthUser operator = authService.requireRole(authorizationHeader, Role.OPERATOR);
		ensureOperatorManagesSchool(operator.userId(), schoolId);
		School school = schoolRepository.findById(schoolId).orElseThrow(() -> new NotFoundException("학교를 찾을 수 없습니다."));
		school.update(request.name(), request.active());
		return SchoolResponse.from(schoolRepository.save(school));
	}

	/** 학교의 학급 목록 — 운영자는 자신이 관리하는 학교의 학급만 조회할 수 있습니다. */
	@GetMapping("/schools/{schoolId}/classrooms")
	public List<ClassroomResponse> getClassrooms(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String schoolId) {
		AuthUser operator = authService.requireRole(authorizationHeader, Role.OPERATOR);
		ensureOperatorManagesSchool(operator.userId(), schoolId);
		return classroomRepository.findBySchoolIdOrderByNameAsc(schoolId).stream().map(ClassroomResponse::from).toList();
	}

	/** 학급 생성 — 운영자는 자신이 관리하는 학교에만 학급을 생성할 수 있습니다. */
	@PostMapping("/schools/{schoolId}/classrooms")
	public ClassroomResponse createClassroom(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String schoolId, @Valid @RequestBody CreateClassroomRequest request) {
		AuthUser operator = authService.requireRole(authorizationHeader, Role.OPERATOR);
		ensureOperatorManagesSchool(operator.userId(), schoolId);
		schoolRepository.findById(schoolId).orElseThrow(() -> new NotFoundException("학교를 찾을 수 없습니다."));
		return ClassroomResponse.from(classroomRepository.save(new Classroom(schoolId, request.name(), request.grade())));
	}

	/** 학급 수정 — 운영자는 자신이 관리하는 학교의 학급만 수정할 수 있습니다. */
	@PatchMapping("/classrooms/{classroomId}")
	public ClassroomResponse updateClassroom(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String classroomId, @Valid @RequestBody UpdateClassroomRequest request) {
		AuthUser operator = authService.requireRole(authorizationHeader, Role.OPERATOR);
		Classroom classroom = classroomRepository.findById(classroomId).orElseThrow(() -> new NotFoundException("학급을 찾을 수 없습니다."));
		ensureOperatorManagesSchool(operator.userId(), classroom.getSchoolId());
		classroom.update(request.name(), request.grade());
		return ClassroomResponse.from(classroomRepository.save(classroom));
	}

	/** 사용자 목록 — 운영자는 자신이 관리하는 학교의 사용자만 조회할 수 있습니다. */
	@GetMapping("/users")
	public List<AdminUserResponse> getUsers(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @RequestParam(required = false) String schoolId) {
		AuthUser operator = authService.requireRole(authorizationHeader, Role.OPERATOR);
		List<String> managedSchoolIds = getManagedSchoolIds(operator);
		String effectiveSchoolId = (schoolId != null && !schoolId.isBlank()) ? schoolId : null;
		if (effectiveSchoolId != null) {
			ensureOperatorManagesSchool(operator.userId(), effectiveSchoolId);
		}
		return authUserRepository.findAll().stream()
			.filter(user -> effectiveSchoolId != null
				? effectiveSchoolId.equals(user.getSchoolId())
				: managedSchoolIds.contains(user.getSchoolId()))
			.map(AdminUserResponse::from)
			.toList();
	}

	/** 사용자 생성 — 운영자는 자신이 관리하는 학교에만 사용자를 생성할 수 있습니다. */
	@PostMapping("/users")
	public AdminUserResponse createUser(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @Valid @RequestBody CreateAdminUserRequest request) {
		AuthUser operator = authService.requireRole(authorizationHeader, Role.OPERATOR);
		ensureOperatorManagesSchool(operator.userId(), request.schoolId());
		ensureClassroomMatchesSchool(request.classroomId(), request.schoolId());
		AuthUserEntity user = new AuthUserEntity(
			request.schoolId(),
			request.classroomId(),
			request.loginId(),
			passwordEncoder.encode(request.password()),
			request.displayName(),
			request.role()
		);
		AuthUserEntity savedUser = authUserRepository.save(user);
		syncOperatorMembership(savedUser.getId(), request.schoolId(), request.role());
		return AdminUserResponse.from(savedUser);
	}

	/** 사용자 수정 — 운영자는 자신이 관리하는 학교의 사용자만 수정할 수 있습니다. */
	@PatchMapping("/users/{userId}")
	public AdminUserResponse updateUser(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String userId, @Valid @RequestBody UpdateAdminUserRequest request) {
		AuthUser operator = authService.requireRole(authorizationHeader, Role.OPERATOR);
		AuthUserEntity user = authUserRepository.findById(userId).orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
		ensureOperatorManagesSchool(operator.userId(), user.getSchoolId());
		ensureOperatorManagesSchool(operator.userId(), request.schoolId());
		ensureClassroomMatchesSchool(request.classroomId(), request.schoolId());
		user.update(request.schoolId(), request.classroomId(), request.displayName(), request.role(), request.active());
		if (request.password() != null && !request.password().isBlank()) {
			user.updatePassword(passwordEncoder.encode(request.password()));
		}
		AuthUserEntity savedUser = authUserRepository.save(user);
		syncOperatorMembership(savedUser.getId(), request.schoolId(), request.role());
		return AdminUserResponse.from(savedUser);
	}

	/** 학교 마스터 데이터 동기화 — 플랫폼 수준 작업으로 모든 운영자가 접근할 수 있습니다. */
	@PostMapping("/schools/sync-master")
	public SchoolMasterSyncResponse syncSchoolMaster(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
		authService.requireRole(authorizationHeader, Role.OPERATOR);
		return schoolMasterSyncService.syncAll();
	}

	/** 운영자 테스트 환경용 채널 정리 — 유지 대상 외 채널은 비활성화하고 없으면 생성합니다. */
	@PostMapping("/schools/{schoolId}/channels/curate-test-set")
	public CurateTestChannelsResponse curateTestChannels(
		@RequestHeader(name = "Authorization", required = false) String authorizationHeader,
		@PathVariable String schoolId,
		@Valid @RequestBody(required = false) CurateTestChannelsRequest request
	) {
		AuthUser operator = authService.requireRole(authorizationHeader, Role.OPERATOR);
		ensureOperatorManagesSchool(operator.userId(), schoolId);
		CurateTestChannelsRequest safeRequest = request == null ? new CurateTestChannelsRequest(List.of()) : request;
		return channelService.curateForTesting(operator, schoolId, safeRequest.normalizedKeepNames());
	}

	/** 운영자가 관리하는 학교 ID 목록을 반환합니다. */
	private List<String> getManagedSchoolIds(AuthUser operator) {
		List<String> membershipSchoolIds = schoolOperatorMembershipRepository.findByUserIdAndActiveTrue(operator.userId())
			.stream().map(SchoolOperatorMembershipEntity::getSchoolId).toList();
		if (!membershipSchoolIds.isEmpty()) {
			return membershipSchoolIds;
		}
		if (operator.schoolId() == null || operator.schoolId().isBlank()) {
			return List.of();
		}
		return List.of(operator.schoolId());
	}

	/** 운영자가 해당 학교의 관리 권한이 있는지 검증합니다. */
	private void ensureOperatorManagesSchool(String operatorUserId, String schoolId) {
		AuthUserEntity operator = authUserRepository.findById(operatorUserId).orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
		if (schoolId != null && schoolId.equals(operator.getSchoolId())) {
			return;
		}
		schoolOperatorMembershipRepository.findBySchoolIdAndUserIdAndActiveTrue(schoolId, operatorUserId)
			.orElseThrow(() -> new ForbiddenException("해당 학교의 관리 권한이 없습니다."));
	}

	private SchoolResponse toSchoolResponse(SchoolMasterEntity schoolMaster) {
		return new SchoolResponse(schoolMaster.getId(), schoolMaster.getName(), schoolMaster.isActive());
	}

	/** 사용자와 학급의 학교 소속이 일치하는지 검증합니다. */
	private void ensureClassroomMatchesSchool(String classroomId, String schoolId) {
		if (classroomId == null || classroomId.isBlank()) {
			return;
		}
		classroomRepository.findByIdAndSchoolId(classroomId, schoolId)
			.orElseThrow(() -> new NotFoundException("해당 학교의 학급을 찾을 수 없습니다."));
	}

	/** 운영자 역할 변경 시 사용자 schoolId 기준으로 membership을 동기화합니다. */
	private void syncOperatorMembership(String userId, String schoolId, Role role) {
		schoolOperatorMembershipRepository.deleteByUserId(userId);
		if (role == Role.OPERATOR) {
			schoolOperatorMembershipRepository.save(new SchoolOperatorMembershipEntity(schoolId, userId));
		}
	}
}
