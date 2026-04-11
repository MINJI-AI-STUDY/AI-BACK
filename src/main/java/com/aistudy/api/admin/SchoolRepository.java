package com.aistudy.api.admin;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepository extends JpaRepository<School, String> {
	java.util.Optional<School> findByName(String name);
}
