package com.aistudy.api.admin;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassroomRepository extends JpaRepository<Classroom, String> {
	List<Classroom> findBySchoolIdOrderByNameAsc(String schoolId);
}
