package com.aistudy.api.auth;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserRepository extends JpaRepository<AuthUserEntity, String> {
	Optional<AuthUserEntity> findByLoginId(String loginId);

	/** 학교 범위 내 학생 코드 존재 여부를 조회합니다. */
	boolean existsBySchoolIdAndStudentCodeAndRole(String schoolId, String studentCode, Role role);

	/** 학교 범위 내 학생 코드로 학생 계정을 조회합니다. */
	Optional<AuthUserEntity> findBySchoolIdAndStudentCodeAndRole(String schoolId, String studentCode, Role role);
}
