package com.aistudy.api.qa.repository;

import com.aistudy.api.qa.model.QALog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QALogRepository extends JpaRepository<QALog, String> {
	List<QALog> findByMaterialIdAndStudentIdOrderByCreatedAtDesc(String materialId, String studentId);
	List<QALog> findByMaterialIdAndSchoolIdOrderByCreatedAtDesc(String materialId, String schoolId);
	long countByMaterialIdAndSchoolId(String materialId, String schoolId);
}
