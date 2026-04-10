package com.aistudy.api.channel.service;

import com.aistudy.api.channel.dto.ChannelEventResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class ChannelEventService {
	private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

	public SseEmitter subscribe(String channelId) {
		SseEmitter emitter = new SseEmitter(0L);
		emitters.computeIfAbsent(channelId, key -> new CopyOnWriteArrayList<>()).add(emitter);
		emitter.onCompletion(() -> emitters.getOrDefault(channelId, List.of()).remove(emitter));
		emitter.onTimeout(() -> emitters.getOrDefault(channelId, List.of()).remove(emitter));
		return emitter;
	}

	public void publish(String channelId, ChannelEventResponse event) {
		for (SseEmitter emitter : emitters.getOrDefault(channelId, List.of())) {
			try {
				emitter.send(SseEmitter.event().name(event.type()).data(event));
			} catch (IOException exception) {
				emitter.complete();
			}
		}
	}
}
