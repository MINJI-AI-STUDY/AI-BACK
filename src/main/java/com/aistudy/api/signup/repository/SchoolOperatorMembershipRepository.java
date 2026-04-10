package com.aistudy.api.signup.repository;

import com.aistudy.api.signup.model.SchoolOperatorMembershipEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolOperatorMembershipRepository extends JpaRepository<SchoolOperatorMembershipEntity, String> {
	Optional<SchoolOperatorMembershipEntity> findBySchoolIdAndUserIdAndActiveTrue(String schoolId, String userId);
}
