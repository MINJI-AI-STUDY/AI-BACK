package com.aistudy.api.auth;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserRepository extends JpaRepository<AuthUserEntity, String> {
	Optional<AuthUserEntity> findByLoginId(String loginId);

	/** 학교 범위 내 동일 이름 학생 계정 존재 여부를 조회합니다. */
	boolean existsBySchoolIdAndDisplayNameAndRole(String schoolId, String displayName, Role role);

	/** 학교 범위 내 동일 이름 학생 계정 목록을 조회합니다. */
	List<AuthUserEntity> findAllBySchoolIdAndDisplayNameAndRole(String schoolId, String displayName, Role role);
}
