package com.aistudy.api.signup.repository;

import com.aistudy.api.signup.model.SchoolMasterEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolMasterRepository extends JpaRepository<SchoolMasterEntity, String> {
	List<SchoolMasterEntity> findTop20ByNameContainingIgnoreCaseOrderByNameAsc(String name);
}
