package com.aistudy.api.channel.repository;

import com.aistudy.api.channel.model.Channel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelRepository extends JpaRepository<Channel, String> {
	List<Channel> findBySchoolIdAndActiveTrueOrderBySortOrderAsc(String schoolId);
	List<Channel> findBySchoolIdOrderBySortOrderAsc(String schoolId);
	Optional<Channel> findByIdAndSchoolId(String id, String schoolId);
}
