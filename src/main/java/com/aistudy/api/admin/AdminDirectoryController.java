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
import com.aistudy.api.auth.AuthUserEntity;
import com.aistudy.api.auth.AuthUserRepository;
import com.aistudy.api.auth.Role;
import com.aistudy.api.signup.dto.SchoolMasterSyncResponse;
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

@RestController
@RequestMapping("/api/operator")
public class AdminDirectoryController {
	private final AuthService authService;
	private final SchoolRepository schoolRepository;
	private final ClassroomRepository classroomRepository;
	private final AuthUserRepository authUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final SchoolMasterSyncService schoolMasterSyncService;

	public AdminDirectoryController(AuthService authService, SchoolRepository schoolRepository, ClassroomRepository classroomRepository, AuthUserRepository authUserRepository, PasswordEncoder passwordEncoder, SchoolMasterSyncService schoolMasterSyncService) {
		this.authService = authService;
		this.schoolRepository = schoolRepository;
		this.classroomRepository = classroomRepository;
		this.authUserRepository = authUserRepository;
		this.passwordEncoder = passwordEncoder;
		this.schoolMasterSyncService = schoolMasterSyncService;
	}

	@GetMapping("/schools")
	public List<SchoolResponse> getSchools(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
		authService.requireRole(authorizationHeader, Role.OPERATOR);
		return schoolRepository.findAll().stream().map(SchoolResponse::from).toList();
	}

	@PostMapping("/schools")
	public SchoolResponse createSchool(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @Valid @RequestBody CreateSchoolRequest request) {
		authService.requireRole(authorizationHeader, Role.OPERATOR);
		return SchoolResponse.from(schoolRepository.save(new School(request.name())));
	}

	@PatchMapping("/schools/{schoolId}")
	public SchoolResponse updateSchool(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String schoolId, @Valid @RequestBody UpdateSchoolRequest request) {
		authService.requireRole(authorizationHeader, Role.OPERATOR);
		School school = schoolRepository.findById(schoolId).orElseThrow(() -> new IllegalArgumentException("학교를 찾을 수 없습니다."));
		school.update(request.name(), request.active());
		return SchoolResponse.from(schoolRepository.save(school));
	}

	@GetMapping("/schools/{schoolId}/classrooms")
	public List<ClassroomResponse> getClassrooms(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String schoolId) {
		authService.requireRole(authorizationHeader, Role.OPERATOR);
		return classroomRepository.findBySchoolIdOrderByNameAsc(schoolId).stream().map(ClassroomResponse::from).toList();
	}

	@PostMapping("/schools/{schoolId}/classrooms")
	public ClassroomResponse createClassroom(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String schoolId, @Valid @RequestBody CreateClassroomRequest request) {
		authService.requireRole(authorizationHeader, Role.OPERATOR);
		return ClassroomResponse.from(classroomRepository.save(new Classroom(schoolId, request.name(), request.grade())));
	}

	@PatchMapping("/classrooms/{classroomId}")
	public ClassroomResponse updateClassroom(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String classroomId, @Valid @RequestBody UpdateClassroomRequest request) {
		authService.requireRole(authorizationHeader, Role.OPERATOR);
		Classroom classroom = classroomRepository.findById(classroomId).orElseThrow(() -> new IllegalArgumentException("학급을 찾을 수 없습니다."));
		classroom.update(request.name(), request.grade());
		return ClassroomResponse.from(classroomRepository.save(classroom));
	}

	@GetMapping("/users")
	public List<AdminUserResponse> getUsers(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @RequestParam(required = false) String schoolId) {
		authService.requireRole(authorizationHeader, Role.OPERATOR);
		return authUserRepository.findAll().stream()
			.filter(user -> schoolId == null || schoolId.isBlank() || schoolId.equals(user.getSchoolId()))
			.map(AdminUserResponse::from)
			.toList();
	}

	@PostMapping("/users")
	public AdminUserResponse createUser(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @Valid @RequestBody CreateAdminUserRequest request) {
		authService.requireRole(authorizationHeader, Role.OPERATOR);
		AuthUserEntity user = new AuthUserEntity(
			request.schoolId(),
			request.classroomId(),
			request.loginId(),
			passwordEncoder.encode(request.password()),
			request.displayName(),
			request.role()
		);
		return AdminUserResponse.from(authUserRepository.save(user));
	}

	@PatchMapping("/users/{userId}")
	public AdminUserResponse updateUser(@RequestHeader(name = "Authorization", required = false) String authorizationHeader, @PathVariable String userId, @Valid @RequestBody UpdateAdminUserRequest request) {
		authService.requireRole(authorizationHeader, Role.OPERATOR);
		AuthUserEntity user = authUserRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		user.update(request.schoolId(), request.classroomId(), request.displayName(), request.role(), request.active());
		if (request.password() != null && !request.password().isBlank()) {
			user.updatePassword(passwordEncoder.encode(request.password()));
		}
		return AdminUserResponse.from(authUserRepository.save(user));
	}

	@PostMapping("/schools/sync-master")
	public SchoolMasterSyncResponse syncSchoolMaster(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
		authService.requireRole(authorizationHeader, Role.OPERATOR);
		return schoolMasterSyncService.syncAll();
	}
}
