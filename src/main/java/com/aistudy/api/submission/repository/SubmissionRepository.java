package com.aistudy.api.submission.repository;

import com.aistudy.api.submission.model.Submission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, String> {
	boolean existsByQuestionSetIdAndStudentId(String questionSetId, String studentId);
	List<Submission> findByQuestionSetId(String questionSetId);
	List<Submission> findByMaterialIdAndSchoolId(String materialId, String schoolId);
	long countDistinctByMaterialIdAndSchoolId(String materialId, String schoolId);
	long countByMaterialIdAndSchoolId(String materialId, String schoolId);
	List<Submission> findBySchoolId(String schoolId);
}
