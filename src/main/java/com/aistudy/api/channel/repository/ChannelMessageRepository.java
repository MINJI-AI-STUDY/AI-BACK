package com.aistudy.api.channel.repository;

import com.aistudy.api.channel.model.ChannelMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelMessageRepository extends JpaRepository<ChannelMessage, String> {
	List<ChannelMessage> findTop50ByChannelIdOrderByCreatedAtDesc(String channelId);
}
