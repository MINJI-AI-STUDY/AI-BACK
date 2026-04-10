package com.aistudy.api.material.repository;

import com.aistudy.api.material.model.Material;
import com.aistudy.api.material.model.MaterialStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, String> {
	Optional<Material> findByIdAndTeacherId(String id, String teacherId);
	Optional<Material> findByIdAndSchoolId(String id, String schoolId);
	List<Material> findBySchoolIdOrderByCreatedAtDesc(String schoolId);
	List<Material> findBySchoolIdAndStatusOrderByCreatedAtDesc(String schoolId, MaterialStatus status);
	List<Material> findByChannelIdOrderByCreatedAtDesc(String channelId);
	List<Material> findByChannelIdAndStatusOrderByCreatedAtDesc(String channelId, MaterialStatus status);
	Optional<Material> findTopBySchoolIdOrderByDocNoDesc(String schoolId);
}
