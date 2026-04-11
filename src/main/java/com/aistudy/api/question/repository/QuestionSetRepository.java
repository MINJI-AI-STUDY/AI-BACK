package com.aistudy.api.question.repository;

import com.aistudy.api.question.model.QuestionSet;
import com.aistudy.api.question.model.QuestionSetStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionSetRepository extends JpaRepository<QuestionSet, String> {
	Optional<QuestionSet> findByDistributionCodeAndStatus(String distributionCode, QuestionSetStatus status);
	List<QuestionSet> findByMaterialIdAndSchoolIdOrderByCreatedAtDesc(String materialId, String schoolId);
	long countByMaterialIdAndSchoolId(String materialId, String schoolId);
	List<QuestionSet> findBySchoolId(String schoolId);
}
