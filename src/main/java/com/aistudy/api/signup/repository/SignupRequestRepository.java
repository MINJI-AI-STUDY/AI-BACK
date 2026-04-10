package com.aistudy.api.signup.repository;

import com.aistudy.api.signup.model.SignupRequestEntity;
import com.aistudy.api.signup.model.SignupStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignupRequestRepository extends JpaRepository<SignupRequestEntity, String> {
	List<SignupRequestEntity> findBySchoolIdAndStatusOrderByCreatedAtDesc(String schoolId, SignupStatus status);
	Optional<SignupRequestEntity> findByIdAndSchoolId(String id, String schoolId);
}
