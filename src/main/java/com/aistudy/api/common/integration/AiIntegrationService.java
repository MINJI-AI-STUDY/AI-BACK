package com.aistudy.api.common.integration;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AiIntegrationService {

	private final RestClient restClient;

	public AiIntegrationService(@Value("${app.ai.base-url}") String aiBaseUrl) {
		this.restClient = RestClient.builder().baseUrl(aiBaseUrl).build();
	}

	/** AI 서버에 자료 추출을 요청합니다. */
	public String extractMaterial(String title, String description, String filename) {
		try {
			Map<String, Object> response = restClient.post()
				.uri("/extract-material")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Map.of("title", title, "description", description, "filename", filename))
				.retrieve()
				.body(Map.class);
			return String.valueOf(response == null ? "" : response.getOrDefault("extractedText", ""));
		} catch (Exception exception) {
			return "AI 추출 실패로 기본 추출 텍스트를 사용합니다.";
		}
	}
}
